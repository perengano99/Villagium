package com.perengano99.villagium.entity;

import net.minecraft.world.entity.LivingEntity;

public class BreastSettings {
	
	private float chestSize = .6f, chestOffsetX, chestOffsetY, chestOffsetZ, chestOutward;
	
	public BreastSettings() {
	}
	
	public float getSize() {
		return chestSize;
	}
	
	public void setSize(float chestSize) {
		this.chestSize = chestSize;
	}
	
	public void setOffsets(float chestOffsetX, float chestOffsetY, float chestOffsetZ) {
		this.chestOffsetX = chestOffsetX;
		this.chestOffsetY = chestOffsetY;
		this.chestOffsetZ = chestOffsetZ;
	}
	
	public void setOutward(float chestOutward) {
		this.chestOutward = chestOutward;
	}
	
	public float getOffsetX() {
		return chestOffsetX;
	}
	
	public float getOffsetY() {
		return chestOffsetY;
	}
	
	public float getOffsetZ() {
		return chestOffsetZ;
	}
	
	public float getOutward() {
		return chestOutward;
	}
	
	public interface IBreastPhysics {
		
		default void tick(LivingEntity entity, BreastSettings settings) {}
		
		IBreastPhysics NO_OP = new IBreastPhysics() {};
	}
}