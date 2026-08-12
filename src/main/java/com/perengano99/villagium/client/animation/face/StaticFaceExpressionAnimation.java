package com.perengano99.villagium.client.animation.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;

public class StaticFaceExpressionAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	private float eyebrowPosY = 0.0f;
	private float eyelidScaleY = 1.0f;
	private float irisPosX = 0.0f;
	private float irisPosY = 0.0f;
	
	public StaticFaceExpressionAnimation() {}
	
	public StaticFaceExpressionAnimation(float eyebrowPosY, float eyelidScaleY, float irisPosX, float irisPosY) {
		this.eyebrowPosY  = eyebrowPosY;
		this.eyelidScaleY = eyelidScaleY;
		this.irisPosX     = irisPosX;
		this.irisPosY     = irisPosY;
	}
	
	public void setEyebrowPosY(float eyebrowPosY) {
		this.eyebrowPosY = eyebrowPosY;
	}
	
	public void setEyelidScaleY(float eyelidScaleY) {
		this.eyelidScaleY = eyelidScaleY;
	}
	
	public void setIrisPos(float posX, float posY) {
		this.irisPosX = posX;
		this.irisPosY = posY;
	}
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, S state, float partialTicks) {
		animator.leftEyebrowTargets.posY += eyebrowPosY;
		animator.rightEyebrowTargets.posY += eyebrowPosY;
		
		animator.leftEyelidTargets.scaleY *= eyelidScaleY;
		animator.rightEyelidTargets.scaleY *= eyelidScaleY;
		
		animator.leftIrisTargets.posX += irisPosX;
		animator.leftIrisTargets.posY += irisPosY;
		animator.rightIrisTargets.posX += irisPosX;
		animator.rightIrisTargets.posY += irisPosY;
	}
}
