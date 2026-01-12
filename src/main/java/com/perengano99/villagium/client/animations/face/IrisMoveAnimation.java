package com.perengano99.villagium.client.animations.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;

public class IrisMoveAnimation<S extends NvHumanoidRenderState> implements IFaceModelAnimation<S> {
	
	private long nextMoveTime = -1;
	private float currentX = 0, currentY = 0;
	private float targetX = 0, targetY = 0;
	private float prevX = 0, prevY = 0;
	private int remainingTicks = 0;
	
	@Override
	public void tick(FaceModelAnimator<S> animator, long gameTime) {
		if (nextMoveTime == -1) scheduleNextMove(gameTime);
		
		// Guardar valores previos para interpolación
		prevX = currentX;
		prevY = currentY;
		
		if (gameTime >= nextMoveTime && remainingTicks <= 0) {
			// Elegir nueva posición objetivo
			targetX        = (getRandom().nextFloat() - 0.5f) * 0.5f; // Rango -0.25 a 0.25
			targetY        = (getRandom().nextFloat() - 0.5f) * 0.2f;  // Rango -0.1 a 0.1
			remainingTicks = 6 + getRandom().nextInt(10); // 6-15 ticks
			scheduleNextMove(gameTime);
		}
		
		if (remainingTicks > 0) {
			currentX += (targetX - currentX) / remainingTicks;
			currentY += (targetY - currentY) / remainingTicks;
			remainingTicks--;
		} else {
			currentX = targetX;
			currentY = targetY;
		}
	}
	
	@Override
	public void animate(FaceModelAnimator<S> animator, float partialTicks) {
		float lerpedX = prevX + (currentX - prevX) * partialTicks;
		float lerpedY = prevY + (currentY - prevY) * partialTicks;
		
		animator.leftIrisTargets.posX += lerpedX;
		animator.rightIrisTargets.posX += lerpedX;
		animator.leftIrisTargets.posY += lerpedY;
		animator.rightIrisTargets.posY += lerpedY;
	}
	
	private void scheduleNextMove(long gameTime) {
		// Próximo movimiento en 1-3 segundos (20-60 ticks)
		this.nextMoveTime = gameTime + 20 + getRandom().nextInt(40);
	}
	
	private RandomSource getRandom() {
		assert Minecraft.getInstance().level != null;
		return Minecraft.getInstance().level.getRandom();
	}
}