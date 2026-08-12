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

public class VillagerRunFemaleAnimation implements ModelAnimation {
	
	private static final AnimationDefinition DEFINITION = Builder.withLength(0.36f)
			.looping()
			.addAnimation("right_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(-105f, -10f, 15f), CATMULLROM),
					new Keyframe(0.18f, degreeVec(105f, -10f, 15f), CATMULLROM),
					new Keyframe(0.36f, degreeVec(-105f, -10f, 15f), CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(105f, 10f, -15f), CATMULLROM),
					new Keyframe(0.18f, degreeVec(-105f, 10f, -15f), CATMULLROM),
					new Keyframe(0.36f, degreeVec(105f, 10f, -15f), CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0f, 0f, 4f), CATMULLROM),
					new Keyframe(0.18f, degreeVec(0f, 0f, -4f), CATMULLROM),
					new Keyframe(0.36f, degreeVec(0f, 0f, 4f), CATMULLROM)
			))
			.build();

	@Override
	public String getId() {
		return "run_female_upper";
	}

	@Override
	public AnimationCategory getCategory() {
		return AnimationCategory.MOVEMENT;
	}

	@Override
	public AnimationDefinition getLoop() {
		return DEFINITION;
	}

	@Override
	public boolean isWalkAnimation() {
		return true;
	}
}
