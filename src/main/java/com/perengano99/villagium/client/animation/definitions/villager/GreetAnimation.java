package com.perengano99.villagium.client.animation.definitions.villager;

import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.client.animation.ModelAnimation;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationDefinition.Builder;
import net.minecraft.client.animation.Keyframe;

import static net.minecraft.client.animation.AnimationChannel.Interpolations.CATMULLROM;
import static net.minecraft.client.animation.AnimationChannel.Targets.ROTATION;
import static net.minecraft.client.animation.KeyframeAnimations.degreeVec;

public class GreetAnimation implements ModelAnimation {
	
	private static final AnimationDefinition DEFINITION = Builder.withLength(1.5f)
			.addAnimation("right_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0f, 0f, 0f), CATMULLROM),
					new Keyframe(0.3f, degreeVec(-110f, 0f, 45f), CATMULLROM),
					new Keyframe(0.6f, degreeVec(-110f, 0f, 75f), CATMULLROM),
					new Keyframe(0.9f, degreeVec(-110f, 0f, 45f), CATMULLROM),
					new Keyframe(1.2f, degreeVec(-110f, 0f, 75f), CATMULLROM),
					new Keyframe(1.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
			))
			.build();

	@Override
	public String getId() {
		return "greet";
	}

	@Override
	public AnimationCategory getCategory() {
		return AnimationCategory.ACTION;
	}

	@Override
	public AnimationDefinition getLoop() {
		return DEFINITION;
	}

	@Override
	public boolean isLoop() {
		return false;
	}
}
