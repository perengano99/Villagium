package com.perengano99.villagium.client.animation.definitions.villager;

import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.client.animation.ModelAnimation;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationDefinition.Builder;
import net.minecraft.client.animation.Keyframe;

import static net.minecraft.client.animation.AnimationChannel.Interpolations.CATMULLROM;
import static net.minecraft.client.animation.AnimationChannel.Targets.POSITION;
import static net.minecraft.client.animation.AnimationChannel.Targets.ROTATION;
import static net.minecraft.client.animation.KeyframeAnimations.degreeVec;
import static net.minecraft.client.animation.KeyframeAnimations.posVec;

public class VillagerMovementLegsAnimation implements ModelAnimation {
	
	public static final AnimationDefinition DEFINITION = Builder.withLength(0.5F).looping()
			.addAnimation("body", new AnimationChannel(POSITION,
					new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(0.0F, 2.2F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(-70.0F, 1.2F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(0.0F, 2.2F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(70.0F, 1.2F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(0.0F, 2.2F, 0.0F), CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(POSITION,
					new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(ROTATION,
					new Keyframe(0.0F, degreeVec(0.0F, -2.2F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, degreeVec(70.0F, -1.2F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, degreeVec(0.0F, -2.2F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, degreeVec(-70.0F, -1.2F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, degreeVec(0.0F, -2.2F, 0.0F), CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(POSITION,
					new Keyframe(0.0F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.125F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.25F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM),
					new Keyframe(0.375F, posVec(0.0F, -0.75F, 0.0F), CATMULLROM),
					new Keyframe(0.5F, posVec(0.0F, 0.0F, 0.0F), CATMULLROM)
			))
			.build();
	
	@Override
	public String getId() {
		return "villager_movement_legs";
	}
	
	@Override
	public AnimationCategory getCategory() {
		return AnimationCategory.MOVEMENT;
	}
	
	@Override
	public AnimationDefinition getLoop() {
		return DEFINITION;
	}
}
