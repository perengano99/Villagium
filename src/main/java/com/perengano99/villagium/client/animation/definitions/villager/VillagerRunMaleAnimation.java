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

public class VillagerRunMaleAnimation implements ModelAnimation {
	
	private static final AnimationDefinition DEFINITION = Builder.withLength(0.5F).looping()
			.addAnimation("head", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(-2.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(-1.0F, 2.0F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(-2.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(-1.0F, -2.0F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(-2.0F, 0.0F, 0.0F), CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(5.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(5.0F, 2.0F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(5.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(5.0F, -2.0F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(5.0F, 0.0F, 0.0F), CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(0.0F, 0.0F, 5.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(50.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(0.0F, 0.0F, 5.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(-50.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(0.0F, 0.0F, 5.0F), CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(0.0F, 0.0F, -5.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(-50.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(0.0F, 0.0F, -5.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(50.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(0.0F, 0.0F, -5.0F), CATMULLROM)
			))
			.build();

	@Override
	public String getId() {
		return "run_male_upper";
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
