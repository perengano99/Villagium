package com.perengano99.villagium.client.animation.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;

public class BlinkAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	private boolean inProgress = false;
	private int remainingTicks = 0;
	private float currentScaleY = 1.0f, targetScaleY = 1.0f;
	private float currentPosY = 0.0f, targetPosY = 0.0f;
	private float prevScaleY = 1.0f, prevPosY = 0.0f;
	private long nextBlinkTime = -1;
	
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {
		if (nextBlinkTime == -1)
			scheduleNextBlink(gameTime);
		
		// Guardar valores previos para interpolación
		prevScaleY = currentScaleY;
		prevPosY   = currentPosY;
		
		if (!inProgress && gameTime >= nextBlinkTime) {
			startBlink(animator);
		} else if (inProgress && remainingTicks <= 0) {
			if (targetScaleY == 0)
				openEyes(gameTime);
			else
				finishBlink(gameTime);
		}
		
		if (remainingTicks > 0) {
			currentScaleY += (targetScaleY - currentScaleY) / (float) remainingTicks;
			currentPosY += (targetPosY - currentPosY) / (float) remainingTicks;
			remainingTicks--;
		} else {
			currentScaleY = targetScaleY;
			currentPosY   = targetPosY;
		}
	}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, float partialTicks) {
		if (!inProgress && currentScaleY == 1.0f) return;
		
		// Interpolación real usando valores previos y partialTicks
		float lerpedScaleY = prevScaleY + (currentScaleY - prevScaleY) * partialTicks;
		float lerpedPosY = prevPosY + (currentPosY - prevPosY) * partialTicks;
		
		animator.leftEyelidTargets.scaleY *= lerpedScaleY;
		animator.rightEyelidTargets.scaleY *= lerpedScaleY;
		
		animator.leftEyelidTargets.posY += lerpedPosY;
		animator.rightEyelidTargets.posY += lerpedPosY;
		
		float irisOffset = lerpedPosY / 1.55f;
		animator.leftIrisTargets.posY += irisOffset;
		animator.rightIrisTargets.posY += irisOffset;
		animator.leftIrisTargets.scaleY *= lerpedScaleY;
		animator.rightIrisTargets.scaleY *= lerpedScaleY;
		
		float eyebrowOffset = lerpedPosY / 1.25f;
		animator.leftEyebrowTargets.posY += eyebrowOffset;
		animator.rightEyebrowTargets.posY += eyebrowOffset;
	}
	
	private void scheduleNextBlink(long gameTime) {
		// Parpadeo cada 80-280 ticks (más natural)
		this.nextBlinkTime = gameTime + 80 + getRandom().nextInt(200);
	}
	
	private void startBlink(FaceModelAnimator<S> animator) {
		inProgress   = true;
		targetScaleY = 0.0f;
		targetPosY   = animator.leftEyelid.baseHeight * animator.leftEyelid.standardScaleY;
		// Cierre rápido: 2-4 ticks
		remainingTicks = 2 + getRandom().nextInt(3);
	}
	
	private void openEyes(long gameTime) {
		targetScaleY = 1.0f;
		targetPosY   = 0.0f;
		// Apertura más lenta: 3-6 ticks
		remainingTicks = 3 + getRandom().nextInt(4);
	}
	
	private void finishBlink(long gameTime) {
		inProgress = false;
		scheduleNextBlink(gameTime);
	}
	
	private RandomSource getRandom() {
		assert Minecraft.getInstance().level != null;
		return Minecraft.getInstance().level.getRandom();
	}
}