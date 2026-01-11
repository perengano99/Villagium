package com.perengano99.villagium.client.renderer.state;

import com.perengano99.villagium.client.animations.BreastModelPhysics;
import com.perengano99.villagium.entity.BreastSettings;
import net.minecraft.world.entity.LivingEntity;

public class BreastPhysicsState implements BreastSettings.IBreastPhysics {
	
	public enum Side { LEFT, RIGHT }
	
	private final BreastModelPhysics l = new BreastModelPhysics();
	private final BreastModelPhysics r = new BreastModelPhysics();
	
	@Override
	public void tick(LivingEntity entity, BreastSettings settings) {
		l.update(entity, settings.getSize());
		r.update(entity, settings.getSize());
	}
	
	public float getPositionY(Side side, float partialTicks) {
		return side == Side.LEFT ? l.getPositionY(partialTicks) : r.getPositionY(partialTicks);
	}
	
	public float getPositionX(Side side, float partialTicks) {
		return side == Side.LEFT ? l.getPositionX(partialTicks) : r.getPositionX(partialTicks);
	}
	
	public float getBounceRotation(Side side, float partialTicks) {
		return side == Side.LEFT ? l.getBounceRotation(partialTicks) : r.getBounceRotation(partialTicks);
	}
}