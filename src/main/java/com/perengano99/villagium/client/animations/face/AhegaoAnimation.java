package com.perengano99.villagium.client.animations.face;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;

public class AhegaoAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	// Estado de la animación
	private boolean inProgress = false;
	private int remainingTicks = 0;
	private long nextTriggerTime = -1;
	
	// Intensidad para interpolación
	private float currentIntensity = 0.0f, targetIntensity = 0.0f, prevIntensity = 0.0f;
	
	// Configuración
	private static final int MIN_INTERVAL = 20 * 20; // 20s
	private static final int MAX_INTERVAL = 30 * 20; // 30s
	private static final int BASE_DURATION = 60;     // 3s
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {
		if (nextTriggerTime == -1) scheduleNext(gameTime);
		
		// Guardar valor previo para interpolación
		prevIntensity = currentIntensity;
		
		if (!inProgress && gameTime >= nextTriggerTime) {
			startExpression(gameTime);
		}
		
		if (inProgress) {
			if (remainingTicks > 0) {
				// Fase de entrada, mantener, salida
				float progress = 1.0f - (remainingTicks / (float) getTotalDuration());
				if (progress < 0.2f) {
					targetIntensity = progress / 0.2f;
				} else if (progress > 0.8f) {
					targetIntensity = 1.0f - ((progress - 0.8f) / 0.2f);
				} else {
					// Vibración sutil
					targetIntensity = 1.0f + (getRandom().nextFloat() - 0.5f) * 0.05f;
				}
				remainingTicks--;
			} else {
				inProgress      = false;
				targetIntensity = 0.0f;
				scheduleNext(gameTime);
				Villagium.LOGGER.debug("Ahegao sequence finished. Cooling down.");
			}
		} else {
			targetIntensity = 0.0f;
		}
		
		// Interpolación suave hacia el target
		currentIntensity += (targetIntensity - currentIntensity) * 0.25f;
		if (currentIntensity < 0.01f) currentIntensity = 0.0f;
	}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, float partialTicks) {
		float lerpedIntensity = prevIntensity + (currentIntensity - prevIntensity) * partialTicks;
		if (lerpedIntensity < 0.01f) return;
		applyTransforms(animator, lerpedIntensity);
	}
	
	private void applyTransforms(FaceModelAnimator<S> controller, float weight) {
		// OJOS: Roll up y cross eyes
		float rollUp = -0.7f * weight; // Antes: -1.8f * weight
		controller.leftIrisTargets.posY += rollUp;
		controller.rightIrisTargets.posY += rollUp;
		
		float crossFactor = 0.6f * weight;
		controller.leftIrisTargets.posX += crossFactor;
		controller.rightIrisTargets.posX -= crossFactor;
		
		controller.leftIrisTargets.scaleX *= (1.0f - (0.1f * weight));
		controller.rightIrisTargets.scaleX *= (1.0f - (0.1f * weight));
		controller.leftIrisTargets.scaleY *= (1.0f - (0.1f * weight));
		controller.rightIrisTargets.scaleY *= (1.0f - (0.1f * weight));
		
		// CEJAS: Suben y se inclinan
		float browUp = -0.8f * weight;
		controller.leftEyebrowTargets.posY += browUp;
		controller.rightEyebrowTargets.posY += browUp;
		
		float browTilt = 0.25f * weight;
		controller.leftEyebrowTargets.rotZ -= browTilt;
		controller.rightEyebrowTargets.rotZ += browTilt;
		
		// PÁRPADOS: Se cierran ligeramente
		float lidClose = 1.0f - (0.2f * weight);
		controller.leftEyelidTargets.scaleY *= lidClose;
		controller.rightEyelidTargets.scaleY *= lidClose;
	}
	
	private void scheduleNext(long currentTime) {
		int delay = getRandom().nextInt(MAX_INTERVAL - MIN_INTERVAL) + MIN_INTERVAL;
		this.nextTriggerTime = currentTime + delay;
		this.remainingTicks  = 0;
		this.targetIntensity = 0.0f;
	}
	
	private void startExpression(long gameTime) {
		this.inProgress       = true;
		this.remainingTicks   = getTotalDuration();
		this.targetIntensity  = 0.0f;
		this.currentIntensity = 0.0f;
		this.prevIntensity    = 0.0f;
	}
	
	private int getTotalDuration() {
		return BASE_DURATION + getRandom().nextInt(40); // 3 a 5 segundos
	}
	
	private RandomSource getRandom() {
		assert Minecraft.getInstance().level != null;
		return Minecraft.getInstance().level.getRandom();
	}
}
