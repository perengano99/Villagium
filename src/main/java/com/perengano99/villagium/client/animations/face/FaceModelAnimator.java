package com.perengano99.villagium.client.animations.face;

import com.perengano99.villagium.client.model.parts.FacePartModel;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FaceModelAnimator<S extends NvHumanoidRenderState> {
	
	public static class AnimationTargets {
		
		public float posX = 0, posY = 0, posZ = 0;
		public float rotX = 0, rotY = 0, rotZ = 0;
		public float scaleX = 1, scaleY = 1, scaleZ = 1;
		
		void reset() {
			posX   = posY = posZ = 0;
			rotX   = rotY = rotZ = 0;
			scaleX = scaleY = scaleZ = 1;
		}
	}
	
	public final FacePartModel leftEyelid, rightEyelid;
	public final FacePartModel leftEyebrow, rightEyebrow;
	public final FacePartModel leftIris, rightIris;
	
	public final AnimationTargets leftEyelidTargets = new AnimationTargets();
	public final AnimationTargets rightEyelidTargets = new AnimationTargets();
	public final AnimationTargets leftEyebrowTargets = new AnimationTargets();
	public final AnimationTargets rightEyebrowTargets = new AnimationTargets();
	public final AnimationTargets leftIrisTargets = new AnimationTargets();
	public final AnimationTargets rightIrisTargets = new AnimationTargets();
	
	private final List<IFaceModelAnimation<S>> activeAnimations = new ArrayList<>();
	private final List<FacePartModel> allParts;
	
	public FaceModelAnimator() {
		leftEyelid  = new FacePartModel(new Vector3f(-2.9f, -0.1f, 0), 1, 0, 2, 2, 0.9f, 0.9f);
		rightEyelid = new FacePartModel(new Vector3f(0.9f, -0.1f, 0), 5, 0, 2, 2, 0.9f, 0.9f);
		
		leftEyebrow  = new FacePartModel(new Vector3f(-3.6f, -0.6f, -.001f), 0, 3, 4, 2, 0.7f, 0.6f);
		rightEyebrow = new FacePartModel(new Vector3f(0.6f, -0.6f, -.001f), 4, 3, 4, 2, 0.7f, 0.6f);
		
		leftIris  = new FacePartModel(new Vector3f(-2.2f, 0.1f, -0.0009f), 9, 0, 2, 2, 0.45f, 0.65f);
		rightIris = new FacePartModel(new Vector3f(1.2f, 0.1f, -0.0009f), 12, 0, 2, 2, 0.45f, 0.65f);
		
		this.allParts = List.of(leftEyelid, rightEyelid, leftEyebrow, rightEyebrow, leftIris, rightIris);
		
		registerAnimation(new BlinkAnimation<>());
		registerAnimation(new IrisMoveAnimation<>());
	}
	
	public Iterable<FacePartModel> getAllParts() {
		return this.allParts;
	}
	
	public void registerAnimation(IFaceModelAnimation<S> animation) {
		this.activeAnimations.add(animation);
	}
	
	public void tick(long gameTime) {
		for (IFaceModelAnimation<S> anim : activeAnimations)
			anim.tick(this, gameTime);
	}
	
	public void update(S state) {
		resetTargets();
		for (IFaceModelAnimation<S> anim : activeAnimations)
			anim.animate(this, state.partialTick);
		
		commitToParts();
		updateParts();
	}
	
	private void resetTargets() {
		leftEyelidTargets.reset();
		rightEyelidTargets.reset();
		leftEyebrowTargets.reset();
		rightEyebrowTargets.reset();
		leftIrisTargets.reset();
		rightIrisTargets.reset();
	}
	
	private void commitToParts() {
		leftEyelid.commitTransforms(leftEyelidTargets);
		rightEyelid.commitTransforms(rightEyelidTargets);
		leftEyebrow.commitTransforms(leftEyebrowTargets);
		rightEyebrow.commitTransforms(rightEyebrowTargets);
		leftIris.commitTransforms(leftIrisTargets);
		rightIris.commitTransforms(rightIrisTargets);
	}
	
	private void updateParts() {
		leftEyelid.update();
		rightEyelid.update();
		leftEyebrow.update();
		rightEyebrow.update();
		leftIris.update();
		rightIris.update();
	}
}
