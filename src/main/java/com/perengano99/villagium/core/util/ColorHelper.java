package com.perengano99.villagium.core.util;

public final class ColorHelper {
	
	private ColorHelper() {}
	
	/**
	 * Convierte un color de RGB a HSL.
	 *
	 * @param r, g, b Valores de 0 a 255.
	 * @return float[] con {hue[0-360], saturation[0-1], lightness[0-1]}
	 */
	public static float[] rgbToHsl(int r, int g, int b) {
		float r_norm = r / 255f;
		float g_norm = g / 255f;
		float b_norm = b / 255f;
		
		float max = Math.max(Math.max(r_norm, g_norm), b_norm);
		float min = Math.min(Math.min(r_norm, g_norm), b_norm);
		
		float h = 0, s, l = (max + min) / 2;
		
		if (max == min) {
			h = s = 0; // achromatic
		} else {
			float d = max - min;
			s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
			if (max == r_norm) {
				h = (g_norm - b_norm) / d + (g_norm < b_norm ? 6 : 0);
			} else if (max == g_norm) {
				h = (b_norm - r_norm) / d + 2;
			} else { // max == b_norm
				h = (r_norm - g_norm) / d + 4;
			}
			h /= 6;
		}
		
		return new float[] { h * 360, s, l };
	}
	
	/**
	 * Convierte un color de HSL a RGB.
	 *
	 * @param h, s, l Valores de hue[0-360], saturation[0-1], lightness[0-1].
	 * @return int[] con {r[0-255], g[0-255], b[0-255]}
	 */
	public static int[] hslToRgb(float h, float s, float l) {
		float r, g, b;
		
		if (s == 0) {
			r = g = b = l; // achromatic
		} else {
			float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
			float p = 2 * l - q;
			h /= 360;
			r = hue2rgb(p, q, h + 1f / 3f);
			g = hue2rgb(p, q, h);
			b = hue2rgb(p, q, h - 1f / 3f);
		}
		
		return new int[] { (int) (r * 255), (int) (g * 255), (int) (b * 255) };
	}
	
	private static float hue2rgb(float p, float q, float t) {
		if (t < 0)
			t += 1;
		if (t > 1)
			t -= 1;
		if (t < 1f / 6f)
			return p + (q - p) * 6 * t;
		if (t < 1f / 2f)
			return q;
		if (t < 2f / 3f)
			return p + (q - p) * (2f / 3f - t) * 6;
		return p;
	}
}
