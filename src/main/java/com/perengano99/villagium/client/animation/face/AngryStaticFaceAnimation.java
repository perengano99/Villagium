package com.perengano99.villagium.client.animation.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.util.Mth;

public class AngryStaticFaceAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, S state, float partialTicks) {
		float time = state.gameTime + partialTicks;
		
		float pulse = Mth.sin(time * 0.12f) * 0.04f;
		float tension = Mth.cos(time * 0.25f) * 0.02f;
		
		float browPosY = 0.55f + pulse;
		animator.leftEyebrowTargets.posY += browPosY;
		animator.rightEyebrowTargets.posY += browPosY;
		
		animator.leftEyebrowTargets.posX += 0.25f;
		animator.rightEyebrowTargets.posX -= 0.25f;
		
		float browTilt = 0.05f + tension;
		animator.leftEyebrowTargets.rotZ += browTilt;
		animator.rightEyebrowTargets.rotZ -= browTilt;
		
		// Tense, narrowed eyelids
		float eyelidScale = 0.90f + pulse * 0.7f;
		animator.leftEyelidTargets.scaleY *= eyelidScale;
		animator.rightEyelidTargets.scaleY *= eyelidScale;
		
		// Focused, intense gaze
		float irisPosY = 0.45f + tension;
		animator.leftIrisTargets.posY += irisPosY;
		animator.rightIrisTargets.posY += irisPosY;
		
		animator.leftIrisTargets.posX += 0.08f;
		animator.rightIrisTargets.posX -= 0.08f;
		animator.leftIrisTargets.scaleY *= eyelidScale * 0.8f;
		animator.rightIrisTargets.scaleY *= eyelidScale  * 0.8f;
	}
}
