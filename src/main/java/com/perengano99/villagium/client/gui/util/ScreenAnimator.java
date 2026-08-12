package com.perengano99.villagium.client.gui.util;

import net.minecraft.util.Util;

public final class ScreenAnimator {
	
	private ScreenAnimator() {}
	
	@FunctionalInterface
	public interface Easing {
		float ease(float t);
		
		Easing EASE_OUT_BACK = t -> {
			float x = t - 1.0f;
			return 1.0f + 2.70158f * x * x * x + 1.70158f * x * x;
		};
		Easing EASE_OUT_CUBIC = t -> 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
		Easing EASE_IN_BACK = t -> 2.70158f * t * t * t - 1.70158f * t * t;
		Easing EASE_IN_CUBIC = t -> t * t * t;
		Easing LINEAR = t -> t;
	}
	
	public static float getProgress(long startTime, long durationMs) {
		if (startTime == -1L)
			return 0.0f;
		long elapsed = Util.getMillis() - startTime;
		return Math.clamp(elapsed / (float) durationMs, 0.0f, 1.0f);
	}
	
	public static float getProgress(long startTime, long durationMs, long delayMs) {
		if (startTime == -1L)
			return 0.0f;
		long elapsed = Util.getMillis() - startTime;
		return Math.clamp((elapsed - delayMs) / (float) durationMs, 0.0f, 1.0f);
	}
	
	public static int animate(int start, int end, float progress, Easing easing) {
		return start + (int) ((end - start) * easing.ease(progress));
	}
	
	public static int animate(int start, int end, long startTime, long durationMs, Easing easing) {
		return animate(start, end, getProgress(startTime, durationMs), easing);
	}
	
	public static int animate(int start, int end, long startTime, long durationMs, long delayMs, Easing easing) {
		return animate(start, end, getProgress(startTime, durationMs, delayMs), easing);
	}
	
	public static int getFadeColor(int color, float progress) {
		int alpha = (color >> 24) & 0xFF;
		int rgb = color & 0xFFFFFF;
		int newAlpha = (int) (alpha * progress);
		return (newAlpha << 24) | rgb;
	}
}
