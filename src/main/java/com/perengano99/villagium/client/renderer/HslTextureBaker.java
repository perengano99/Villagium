package com.perengano99.villagium.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.core.util.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class HslTextureBaker {
	
	// --- Constantes de la Paleta de Grises ---
	// Define los puntos de control para los efectos, basados en la paleta proporcionada.
	private static final float GRAY_HIGHLIGHT_MAX = 252f / 255f; // #FCFCFC -> Brillo máximo
	private static final float GRAY_BASE = 207f / 255f; // #E6E6E6 -> Punto neutro, sin efecto
	private static final float GRAY_SHADOW_MAX = 48f / 255f;  // #303030 -> Sombra máxima
	
	
	private static final int CACHE_MAX_SIZE = 128;
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
	
	public static Identifier getBakedTexture(int color, @NonNull Identifier baseTexture) {
		// Cache Hit O(1) - Fast Path
		TextureCacheKey key = new TextureCacheKey(color, baseTexture);
		var cached = cache.get(key);
		if (cached != null) return cached;
		
		// Cache Miss - Baking Time
		try {
			Identifier bakedId = bakeTexture(color, baseTexture);
			cache.put(key, bakedId);
			return bakedId;
		} catch (IOException e) {
			Villagium.LOGGER.error("Failed to bake texture: {}", baseTexture, e);
			return baseTexture; // Fail-safe: retorna la original (gris) para no crashear
		}
	}
	
	private static Identifier bakeTexture(int color, Identifier texturePath) throws IOException {
		try (InputStream inputStream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texturePath).open();
		     NativeImage baseImage = NativeImage.read(inputStream)) {
			
			NativeImage targetImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
			
			// Pre-cálculo del color base HSL
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			float[] baseHsl = ColorHelper.rgbToHsl(r, g, b);
			
			float baseHue = baseHsl[0];
			float baseSat = baseHsl[1];
			float baseLight = baseHsl[2];
			
			// Pixel Loop - High Performance Zone
			for (int y = 0; y < baseImage.getHeight(); y++) {
				for (int x = 0; x < baseImage.getWidth(); x++) {
					int pixel = baseImage.getPixel(x, y);
					int alpha = (pixel >> 24) & 0xFF;
					
					if (alpha == 0) {
						targetImage.setPixel(x, y, 0); // Transparente
						continue;
					}
					
					// Leemos luminosidad del canal R (grayscale puro)
					float grayscale = (pixel & 0xFF) / 255f;
					
					// Lógica de Sombras/Luces (Interpolación lineal)
					float highlightFactor = 0f;
					float shadowFactor = 0f;
					
					if (grayscale > GRAY_BASE)
						highlightFactor = Mth.clamp((grayscale - GRAY_BASE) / (GRAY_HIGHLIGHT_MAX - GRAY_BASE), 0f, 1f);
					else
						shadowFactor = Mth.clamp((GRAY_BASE - grayscale) / (GRAY_BASE - GRAY_SHADOW_MAX), 0f, 1f);
					
					// 1. Hue Shift (Temperatura de color)
					// Sombras frías (-Hue), Luces cálidas (+Hue)
					float currentHue = baseHue - (30.0f * shadowFactor) + (15.0f * highlightFactor);
					if (currentHue < 0) currentHue += 360f;
					if (currentHue >= 360) currentHue -= 360f;
					
					// 2. Saturation Shift
					// Menos saturación en sombras y luces extremas
					float currentSat = baseSat * (1.0f - (0.40f * shadowFactor) - (0.1f * highlightFactor));
					currentSat = Mth.clamp(currentSat, 0f, 1f);
					
					// 3. Lightness Shift (Contrast Curve)
					float currentLight = baseLight + (0.30f * highlightFactor) - (0.50f * shadowFactor);
					currentLight = Mth.clamp(currentLight, 0f, 1f);
					
					// Convertir a RGB int directamente (Sin arrays basura)
					int rgb = ColorHelper.hslToRgbInt(currentHue, currentSat, currentLight);
					
					// Combinar con Alpha original
					targetImage.setPixel(x, y, (alpha << 24) | (rgb & 0x00FFFFFF));
				}
			}
			
			// Registrar textura dinámica
			String nameId = texturePath.getPath().replace("/", "_").replace(".png", "") + "_" + Integer.toHexString(color);
			Identifier outputId = Identifier.fromNamespaceAndPath(Villagium.MODID, "dynamic/" + nameId);
			Minecraft.getInstance().getTextureManager().register(outputId, new DynamicTexture(outputId::toString, targetImage));
			return outputId;
		}
	}
}
