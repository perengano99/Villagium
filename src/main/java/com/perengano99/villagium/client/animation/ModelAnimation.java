package com.perengano99.villagium.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import org.jetbrains.annotations.Nullable;

public interface ModelAnimation {
	String getId();
	AnimationCategory getCategory();
	
	@Nullable
	default AnimationDefinition getIntro() {
		return null;
	}
	
	@Nullable
	default AnimationDefinition getLoop() {
		return null;
	}
	
	@Nullable
	default AnimationDefinition getOutro() {
		return null;
	}
	
	default boolean isWalkAnimation() {
		return false;
	}
	
	default boolean cancelsBaseWalk() {
		return false;
	}
	
	default boolean isLoop() {
		return true;
	}
}
