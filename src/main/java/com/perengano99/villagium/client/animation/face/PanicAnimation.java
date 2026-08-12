package com.perengano99.villagium.client.animation.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;

public class PanicAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	private float currentIntensity = 0.0f;
	private float prevIntensity = 0.0f;
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {
		prevIntensity = currentIntensity;
		
		// Access panicking state from animator owner through render state in update.
		// For tick, we just smoothly decay/interpolate intensity based on what was computed.
	}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, S state, float partialTicks) {
		// Update target intensity based on state panicking flag
		float target = state.isPanicking ? 1.0f : 0.0f;
		currentIntensity += (target - currentIntensity) * 0.2f; // Smooth transition
		
		float lerpedIntensity = prevIntensity + (currentIntensity - prevIntensity) * partialTicks;
		if (lerpedIntensity < 0.01f) return;
		
		RandomSource random = state.levelRandom != null ? state.levelRandom : Minecraft.getInstance().level.getRandom();
		
		// 1. CEJAS: Suben y se arquean con miedo (inclinación opuesta al enfado)
		float browLift = -0.5f * lerpedIntensity;
		animator.leftEyebrowTargets.posY += browLift;
		animator.rightEyebrowTargets.posY += browLift;
		
		float browTilt = 0.15f * lerpedIntensity;
		animator.leftEyebrowTargets.rotZ += browTilt; // ceja izquierda se levanta del medio
		animator.rightEyebrowTargets.rotZ -= browTilt;
		
		// 2. PARPADOS: Se abren más de lo normal (escala Y mayor o posición Y menor)
		float eyelidOpen = -0.2f * lerpedIntensity;
		animator.leftEyelidTargets.posY += eyelidOpen;
		animator.rightEyelidTargets.posY += eyelidOpen;
		
		// 3. IRIS: Tiemblan de miedo
		float jitterX = (random.nextFloat() - 0.5f) * 0.15f * lerpedIntensity;
		float jitterY = (random.nextFloat() - 0.5f) * 0.15f * lerpedIntensity;
		animator.leftIrisTargets.posX += jitterX;
		animator.rightIrisTargets.posX += jitterX;
		animator.leftIrisTargets.posY += jitterY;
		animator.rightIrisTargets.posY += jitterY;
	}
}
