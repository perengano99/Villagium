package com.perengano99.villagium.client.animation.definitions;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationDefinition.Builder;

public class HumanoidAnimation {
	
	public static final AnimationDefinition FEMALE_IDLE;
	
	
	static {
		// Idle anim. Primero unicamente el femenino ya que esta configurado fijo.
		FEMALE_IDLE = Builder.withLength();
	}
}
