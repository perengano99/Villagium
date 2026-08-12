package com.perengano99.villagium.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.world.entity.AnimationState;

public record ActiveAnimationRenderState(
		String id,
		AnimationDefinition definition,
		AnimationState state,
		int phaseTicksElapsed,
		boolean isWalk,
		boolean cancelsBaseWalk,
		AnimationCategory category,
		boolean isManual,
		float speedFactor
) {
}
