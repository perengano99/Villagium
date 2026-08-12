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

public class FemaleConversationPoseAnimation implements ModelAnimation {
	
	private static final AnimationDefinition DEFINITION = Builder.withLength(1.0f)
			.looping()
			.addAnimation("right_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(-30.0f, -40.0f, 20.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(-30.0f, -40.0f, 20.0f), CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(15.0f, -10.0f, -5.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(15.0f, -10.0f, -5.0f), CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0.0f, 5.0f, 2.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(0.0f, 5.0f, 2.0f), CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0.0f, -5.0f, 5.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(0.0f, -5.0f, 5.0f), CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0.0f, 0.0f, 2.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(0.0f, 0.0f, 2.0f), CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(ROTATION,
					new Keyframe(0.0f, degreeVec(0.0f, 0.0f, -2.0f), CATMULLROM),
					new Keyframe(1.0f, degreeVec(0.0f, 0.0F, -2.0f), CATMULLROM)
			))
			.build();

	@Override
	public String getId() {
		return "female_conversation_pose";
	}

	@Override
	public AnimationCategory getCategory() {
		return AnimationCategory.POSE;
	}

	@Override
	public AnimationDefinition getLoop() {
		return DEFINITION;
	}
}
