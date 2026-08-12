package com.perengano99.villagium.client.animation;

import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface AnimationRule {
	/**
	 * Returns the weight/priority of the animation for the given entity context.
	 * A weight <= 0.0f means the animation should not play.
	 * Under each AnimationCategory, the rule returning the highest weight > 0 wins.
	 */
	float getWeight(LivingEntity entity, ModelAnimationController controller);
}
