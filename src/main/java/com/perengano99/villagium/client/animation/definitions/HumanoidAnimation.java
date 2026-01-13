package com.perengano99.villagium.client.animation.definitions;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationDefinition.Builder;
import net.minecraft.client.animation.Keyframe;

import static net.minecraft.client.animation.KeyframeAnimations.posVec;
import static net.minecraft.client.animation.KeyframeAnimations.degreeVec;
import static net.minecraft.client.animation.AnimationChannel.Interpolations.CATMULLROM;
import static net.minecraft.client.animation.AnimationChannel.Interpolations.LINEAR;
import static net.minecraft.client.animation.AnimationChannel.Targets.POSITION;
import static net.minecraft.client.animation.AnimationChannel.Targets.ROTATION;

public class HumanoidAnimation {
	
	public static final AnimationDefinition IDLE;
	public static final AnimationDefinition WALK_LOWER;
	public static final AnimationDefinition WALK_FEMALE_UPPER;
	
	static {
		IDLE              = idle();
		WALK_LOWER        = walkLower();
		WALK_FEMALE_UPPER = walkFemaleUpper();
	}
	
	private static AnimationDefinition idle() {
		return Builder.withLength(3.5f)
				.looping()
				.addAnimation("body",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(0.875f, degreeVec(-0.4f, 0.5f, 0f), CATMULLROM),
								new Keyframe(1.75f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(2.625f, degreeVec(-0.4f, -0.5f, 0f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				.addAnimation("body",
						new AnimationChannel(POSITION,
								new Keyframe(0f, posVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(1.75f, posVec(0f, -0.12f, 0f), CATMULLROM),
								new Keyframe(3.5f, posVec(0f, 0f, 0f), CATMULLROM)
						))
				// --- PIERNAS (Corrección de Cintura Sutil) ---
				.addAnimation("right_leg",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(1.75f, degreeVec(0.8f, 0f, 0f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				.addAnimation("left_leg",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(1.75f, degreeVec(0.8f, 0f, 0f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				// --- CABEZA (Estabilización Sutil) ---
				.addAnimation("head",
						new AnimationChannel(POSITION,
								new Keyframe(0f, posVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(1.75f, posVec(0f, -0.12f, 0f), CATMULLROM),
								new Keyframe(3.5f, posVec(0f, 0f, 0f), CATMULLROM)
						))
				.addAnimation("head",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(0.875f, degreeVec(0.2f, -0.5f, 0f), CATMULLROM),
								new Keyframe(1.75f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(2.625f, degreeVec(0.2f, 0.5f, 0f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				// --- BRAZOS (Inercia mínima) ---
				.addAnimation("right_arm",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(1.5f, degreeVec(0.5f, 0f, 0.3f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				.addAnimation("left_arm",
						new AnimationChannel(ROTATION,
								new Keyframe(0f, degreeVec(0f, 0f, 0f), CATMULLROM),
								new Keyframe(2.0f, degreeVec(0.5f, 0f, -0.3f), CATMULLROM),
								new Keyframe(3.5f, degreeVec(0f, 0f, 0f), CATMULLROM)
						))
				.build();
	}
	
	private static AnimationDefinition walkLower() {
		return Builder.withLength(0.48f)
				.looping()
				.addAnimation("right_leg", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(70f, 5.7f, 0f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(-70f, 5.7f, 0f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(70f, 5.7f, 0f), CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(-70f, 5.7f, 0f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(70f, 5.7f, 0f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(-70f, 5.7f, 0f), CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(POSITION,
						new Keyframe(0.0f, posVec(0f, -2.5f, 0f), CATMULLROM),    // Bajar más
						new Keyframe(0.12f, posVec(0f, 0.5f, 0f), CATMULLROM),    // Subir
						new Keyframe(0.24f, posVec(0f, -2.5f, 0f), CATMULLROM),
						new Keyframe(0.36f, posVec(0f, 0.5f, 0f), CATMULLROM),
						new Keyframe(0.48f, posVec(0f, -2.5f, 0f), CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(0f, 0f, 2.8f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(0f, 0f, -2.8f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(0f, 0f, 2.8f), CATMULLROM)
				))
				.build();
	}
	
	private static AnimationDefinition walkFemaleUpper() {
		return Builder.withLength(0.48f)
				.looping()
				.addAnimation("right_arm", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(-91f, -5f, 12f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(91f, -5f, 12f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(-91f, -5f, 12f), CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(91f, 5f, -12f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(-91f, 5f, -12f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(91f, 5f, -12f), CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(POSITION,
						new Keyframe(0.0f, posVec(0f, -0.7f, 0f), CATMULLROM),
						new Keyframe(0.24f, posVec(0f, -0.7f, 0f), CATMULLROM),
						new Keyframe(0.48f, posVec(0f, -0.7f, 0f), CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(POSITION,
						new Keyframe(0.0f, posVec(0f, -0.7f, 0f), CATMULLROM),
						new Keyframe(0.24f, posVec(0f, -0.7f, 0f), CATMULLROM),
						new Keyframe(0.48f, posVec(0f, -0.7f, 0f), CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(0f, 0f, 0f), CATMULLROM),
						new Keyframe(0.12f, degreeVec(0f, 11.5f, 0f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(0f, 0f, 0f), CATMULLROM),
						new Keyframe(0.36f, degreeVec(0f, -11.5f, 0f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(0f, 0f, 0f), CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(POSITION,
						new Keyframe(0.0f, posVec(0f, -2.5f, 0f), CATMULLROM),
						new Keyframe(0.12f, posVec(0f, 0.5f, 0f), CATMULLROM),
						new Keyframe(0.24f, posVec(0f, -2.5f, 0f), CATMULLROM),
						new Keyframe(0.36f, posVec(0f, 0.5f, 0f), CATMULLROM),
						new Keyframe(0.48f, posVec(0f, -2.5f, 0f), CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(ROTATION,
						new Keyframe(0.0f, degreeVec(0f, 0f, -8f), CATMULLROM),
						new Keyframe(0.12f, degreeVec(0f, -3f, 0f), CATMULLROM),
						new Keyframe(0.24f, degreeVec(0f, 0f, 8f), CATMULLROM),
						new Keyframe(0.36f, degreeVec(0f, 3f, 0f), CATMULLROM),
						new Keyframe(0.48f, degreeVec(0f, 0f, -8f), CATMULLROM)
				))
				.build();
	}
}
