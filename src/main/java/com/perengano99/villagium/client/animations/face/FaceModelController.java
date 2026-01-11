package com.perengano99.villagium.client.animations.face;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.model.parts.FacePartModel;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import org.joml.Vector3f;

public class FaceModelController<S extends NvHumanoidRenderState> {
	
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
	
	private final AnimationTargets leftEyelidTargets = new AnimationTargets();
	private final AnimationTargets rightEyelidTargets = new AnimationTargets();
	private final AnimationTargets leftEyebrowTargets = new AnimationTargets();
	private final AnimationTargets rightEyebrowTargets = new AnimationTargets();
	private final AnimationTargets leftIrisTargets = new AnimationTargets();
	private final AnimationTargets rightIrisTargets = new AnimationTargets();
	
	public FaceModelController() {
		leftEyelid  = new FacePartModel(new Vector3f(-2.9f, -0.1f, 0), 1, 0, 2, 2, 0.9f, 0.9f);
		rightEyelid = new FacePartModel(new Vector3f(0.9f, -0.1f, 0), 5, 0, 2, 2, 0.9f, 0.9f);
		
		leftEyebrow  = new FacePartModel(new Vector3f(-3.6f, -0.6f, -.001f), 0, 3, 4, 2, 0.7f, 0.6f);
		rightEyebrow = new FacePartModel(new Vector3f(0.6f, -0.6f, -.001f), 4, 3, 4, 2, 0.7f, 0.6f);
		
		leftIris  = new FacePartModel(new Vector3f(-2.2f, 0.1f, -0.0009f), 9, 0, 2, 2, 0.45f, 0.65f);
		rightIris = new FacePartModel(new Vector3f(1.2f, 0.1f, -0.0009f), 12, 0, 2, 2, 0.45f, 0.65f);
	}
	
	public void update(S state) {
		// --- Proceso de Animación Aditiva (hay que limpiar esto) ---
		
		// 1. Limpiar el "lienzo" de objetivos para este frame.
		leftEyelidTargets.reset();
		rightEyelidTargets.reset();
		leftEyebrowTargets.reset();
		rightEyebrowTargets.reset();
		leftIrisTargets.reset();
		rightIrisTargets.reset();
		
		// 2. Aplicar las animaciones en capas, cada una modificando el lienzo de objetivos.
		applyBlinkingAnimation(state);
		
		// 3. "Comprometer" los objetivos finales a las partes faciales.
		leftEyelid.commitTransforms(leftEyelidTargets);
		rightEyelid.commitTransforms(rightEyelidTargets);
		leftEyebrow.commitTransforms(leftEyebrowTargets);
		rightEyebrow.commitTransforms(rightEyebrowTargets);
		leftIris.commitTransforms(leftIrisTargets);
		rightIris.commitTransforms(rightIrisTargets);
		
		// 4. Actualizar las partes, lo que copia los valores 'current' a 'previous' para la siguiente interpolación.
		leftEyelid.update();
		rightEyelid.update();
		leftEyebrow.update();
		rightEyebrow.update();
		leftIris.update();
		rightIris.update();
	}
	
	// --- Variables de estado de la animación de parpadeo ---
	private boolean blinkInProgress = false;
	// Estado local de la animación de parpadeo, en lugar de un animador externo.
	private float blinkCurrentScaleY = 1.0f;
	private float blinkTargetScaleY = 1.0f;
	private int blinkRemainingTicks = 0;
	private float blinkCurrentPosY = 0.0f;
	private float blinkTargetPosY = 0.0f;
	private long nextBlinkTime = -1;
	
	private void applyBlinkingAnimation(S state) {
		if (nextBlinkTime == -1)
			scheduleNextBlink(state);
		
		long time = state.gameTime;
		
		if (!blinkInProgress && time >= this.nextBlinkTime) {
			blinkInProgress          = true;
			this.blinkTargetScaleY   = 0.0f;
			this.blinkTargetPosY     = this.leftEyelid.baseHeight * this.leftEyelid.standardScaleY;
			this.blinkRemainingTicks = state.getRandom().nextInt(7, 28);
		} else if (blinkInProgress && this.blinkRemainingTicks <= 0) {
			if (this.blinkTargetScaleY == 0.0f) {
				this.blinkTargetScaleY   = 1.0f;
				this.blinkTargetPosY     = 0.0f;
				this.blinkRemainingTicks = state.getRandom().nextInt(7, 28);
			} else {
				blinkInProgress = false;
				this.scheduleNextBlink(state);
				Villagium.LOGGER.info("Applying blinking animation done, schedule in: {}", this.nextBlinkTime - time);
			}
		}
		
		if (this.blinkRemainingTicks > 0) {
			this.blinkCurrentScaleY += (this.blinkTargetScaleY - this.blinkCurrentScaleY) / this.blinkRemainingTicks;
			this.blinkCurrentPosY += (this.blinkTargetPosY - this.blinkCurrentPosY) / this.blinkRemainingTicks;
			this.blinkRemainingTicks--;
		} else {
			this.blinkCurrentScaleY = this.blinkTargetScaleY;
			this.blinkCurrentPosY   = this.blinkTargetPosY;
		}
		
		if (blinkInProgress) {
			leftEyelidTargets.scaleY *= this.blinkCurrentScaleY;
			rightEyelidTargets.scaleY *= this.blinkCurrentScaleY;
			leftEyelidTargets.posY += this.blinkCurrentPosY;
			rightEyelidTargets.posY += this.blinkCurrentPosY;
			
			leftIrisTargets.scaleY *= blinkCurrentScaleY;
			rightIrisTargets.scaleY *= blinkCurrentScaleY;
			leftIrisTargets.posY += blinkCurrentPosY / 1.55f;
			rightIrisTargets.posY += blinkCurrentPosY / 1.55f;
			
			leftEyebrowTargets.posY += blinkCurrentPosY / 1.25f;
			rightEyebrowTargets.posY += blinkCurrentPosY / 1.25f;
		}
	}
	
	private void scheduleNextBlink(S state) {
		// Parpadea aleatoriamente cada 4-10 segundos (40 a 140 ticks)
		
		this.nextBlinkTime = state.gameTime + 80 + state.getRandom().nextInt(200);
		Villagium.LOGGER.info("scheduleNextBlink: nextBlinkTime set to {}", this.nextBlinkTime);
	}
}
