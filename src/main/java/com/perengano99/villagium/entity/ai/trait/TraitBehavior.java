package com.perengano99.villagium.entity.ai.trait;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;

public abstract class TraitBehavior {
	protected TraitBehavior() {}

	public abstract BehaviorControl<?> getBehavior();
}
