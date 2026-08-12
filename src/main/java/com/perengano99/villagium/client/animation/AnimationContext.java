package com.perengano99.villagium.client.animation;

public record AnimationContext(
		boolean isFemale,
		boolean isBaby,
		boolean isPanicking,
		boolean isRunning,
		boolean useAltIdle
) {
}
