package com.perengano99.villagium.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.core.util.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DynamicColorTextureManager {
	
	// --- Constantes de la Paleta de Grises ---
	// Define los puntos de control para los efectos, basados en la paleta proporcionada.
	private static final float GRAY_HIGHLIGHT_MAX = 252f / 255f; // #FCFCFC -> Brillo máximo
	private static final float GRAY_BASE = 207f / 255f; // #E6E6E6 -> Punto neutro, sin efecto
	private static final float GRAY_SHADOW_MAX = 48f / 255f;  // #303030 -> Sombra máxima
	
	
	private static final int CACHE_MAX_SIZE = 256;
	private static final Map<TextureCacheKey, Identifier> cache = new LinkedHashMap<>(CACHE_MAX_SIZE, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<TextureCacheKey, Identifier> eldest) {
			return size() > CACHE_MAX_SIZE;
		}
	};
	
	private record TextureCacheKey(int color, Identifier texture) {}
	
	public static void clearCache() {
		cache.clear();
	}
	
	public static Identifier getColoredTexture(int color, @NonNull Identifier texture) {
		final TextureCacheKey key = new TextureCacheKey(color, texture);
		if (cache.containsKey(key)) return cache.get(key);
		
		try {
			final NativeImage base;
			try (InputStream inputStream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture).open()) {
				base = NativeImage.read(inputStream);
			}
			
			NativeImage colored = new NativeImage(base.getWidth(), base.getHeight(), true);
			
			int r_ = (color >> 16) & 0xFF;
			int g_ = (color >> 8) & 0xFF;
			int b_ = color & 0xFF;
			float[] hslBase = ColorHelper.rgbToHsl(r_, g_, b_);
			
			for (int y = 0; y < base.getHeight(); y++) {
				for (int x = 0; x < base.getWidth(); x++) {
					final int pxColor = base.getPixel(x, y);
					final int a = (pxColor >> 24) & 0xFF;
					if (a == 0) continue;
					
					// El valor de gris de la textura base. Lo leemos del canal rojo (R=G=B).
					final float grayscale = (pxColor & 0xFF) / 255f;
					
					// --- Factores de Sombra y Brillo ---
					// Se calculan interpolando entre los puntos clave de la paleta de grises.
					// Un valor de 0 significa sin efecto, 1 es el efecto máximo.
					
					// El factor de brillo se interpola entre el color BASE y el BRILLO_MAX.
					float highlightFactor = 0f;
					if (grayscale > GRAY_BASE) highlightFactor = (grayscale - GRAY_BASE) / (GRAY_HIGHLIGHT_MAX - GRAY_BASE);
					highlightFactor = Math.min(1f, highlightFactor); // Clamp para valores por encima del máximo
					
					// El factor de sombra se interpola entre el color BASE y la SOMBRA_MAX.
					float shadowFactor = 0f;
					if (grayscale < GRAY_BASE) shadowFactor = (GRAY_BASE - grayscale) / (GRAY_BASE - GRAY_SHADOW_MAX);
					shadowFactor = Math.min(1f, shadowFactor); // Clamp para valores por debajo del mínimo
					
					float hue = hslBase[0];
					float saturation = hslBase[1];
					float lightness = hslBase[2];
					
					// 1. Ajuste de Matiz (Hue)
					hue -= 30.0f * shadowFactor; // Sombras se enfrían (hacia el azul)
					hue += 15.0f * highlightFactor; // Brillos se calientan (hacia el amarillo)
					
					// 2. Ajuste de Saturación
					// Se reduce en sombras (más grisáceo) y ligeramente en brillos (luz intensa).
					float saturationReduction = (0.40f * shadowFactor) + (0.1f * highlightFactor);
					saturation *= (1.0f - saturationReduction);
					
					// 3. Ajuste de Luminosidad
					// El cambio clave: partimos de la luminosidad original y la aumentamos o reducimos.
					float lightnessAdjustment = (0.30f * highlightFactor) - (0.50f * shadowFactor);
					lightness += lightnessAdjustment;
					
					// 4. Normalización de valores HSL
					// Asegurar que el matiz esté en el rango [0, 360)
					if (hue < 0) hue += 360f;
					if (hue >= 360) hue -= 360f;
					
					// Asegurar que la saturación y luminosidad estén en el rango [0, 1]
					saturation = Math.max(0f, Math.min(1.0f, saturation));
					lightness  = Math.max(0f, Math.min(1.0f, lightness));
					
					int[] finalRgb = ColorHelper.hslToRgb(hue, saturation, lightness);
					
					// Escribir el pixel final en la nueva imagen
					// OJO: NativeImage usa el formato ABGR
					int finalColor = (a << 24) | (finalRgb[0] << 16) | (finalRgb[1] << 8) | finalRgb[2];
					colored.setPixel(x, y, finalColor);
				}
			}
			
			Identifier identifier = Identifier.fromNamespaceAndPath(Villagium.MODID, "dynamic_image/" + texture.getPath() + "_" + Integer.toHexString(color));
			DynamicTexture dynamicTexture = new DynamicTexture(identifier::toString, colored);
			Minecraft.getInstance().getTextureManager().register(identifier, dynamicTexture);
			cache.put(key, identifier);
			
			base.close();
			return identifier;
		} catch (IOException e) {
			return texture;
		}
	}
}
