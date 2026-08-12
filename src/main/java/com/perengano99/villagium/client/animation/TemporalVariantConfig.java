package com.perengano99.villagium.client.animation;

public record TemporalVariantConfig(
		int checkIntervalTicks,
		float activationChance,
		int minDurationTicks,
		int maxDurationTicks
) {
	public static final TemporalVariantConfig DEFAULT = new TemporalVariantConfig(100, 0.2f, 120, 200);
}
