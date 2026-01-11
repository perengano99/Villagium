package com.perengano99.villagium.client.model.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.perengano99.villagium.client.animations.face.FaceModelController;
import com.perengano99.villagium.client.model.Box;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class FacePartModel extends Box {
	
	public final Vector3f pivot;
	
	public final float baseWidth, baseHeight;
	public final float standardScaleX, standardScaleY;
	
	private float currentPosX, previousPosX;
	private float currentPosY, previousPosY;
	private float currentPosZ, previousPosZ;
	private float currentRotX, previousRotX;
	private float currentRotY, previousRotY;
	private float currentRotZ, previousRotZ;
	private float currentScaleX, previousScaleX;
	private float currentScaleY, previousScaleY;
	private float currentScaleZ, previousScaleZ;
	
	public FacePartModel(Vector3f pivot, int texU, int texV, int width, int height, float standardScaleX, float standardScaleY) {
		super(32, 32, texU, texV, 0, 0, 0, width, height, 0, 0, false);
		baseWidth           = width;
		baseHeight          = height;
		this.pivot          = pivot;
		this.standardScaleX = standardScaleX;
		this.standardScaleY = standardScaleY;
		
		this.currentScaleX = this.previousScaleX = 1;
		this.currentScaleY = this.previousScaleY = 1;
		this.currentScaleZ = this.previousScaleZ = 1;
	}
	
	public void commitTransforms(FaceModelController.AnimationTargets targets) {
		this.currentPosX   = targets.posX;
		this.currentPosY   = targets.posY;
		this.currentPosZ   = targets.posZ;
		this.currentRotX   = targets.rotX;
		this.currentRotY   = targets.rotY;
		this.currentRotZ   = targets.rotZ;
		this.currentScaleX = targets.scaleX;
		this.currentScaleY = targets.scaleY;
		this.currentScaleZ = targets.scaleZ;
	}
	
	public void update() {
		this.previousPosX   = this.currentPosX;
		this.previousPosY   = this.currentPosY;
		this.previousPosZ   = this.currentPosZ;
		this.previousRotX   = this.currentRotX;
		this.previousRotY   = this.currentRotY;
		this.previousRotZ   = this.currentRotZ;
		this.previousScaleX = this.currentScaleX;
		this.previousScaleY = this.currentScaleY;
		this.previousScaleZ = this.currentScaleZ;
	}
	
	public void applyTransformations(PoseStack poseStack, float partialTicks) {
		// --- 1. Calcular la escala final ---
		float animScaleX = Mth.lerp(partialTicks, previousScaleX, currentScaleX);
		float animScaleY = Mth.lerp(partialTicks, previousScaleY, currentScaleY);
		float animScaleZ = Mth.lerp(partialTicks, previousScaleZ, currentScaleZ);
		float finalScaleX = this.standardScaleX * animScaleX;
		float finalScaleY = this.standardScaleY * animScaleY;
		
		// --- 2. Calcular la corrección del pivote para anular el "desfase" ---
		poseStack.translate(pivot.x() / 16.0f, pivot.y() / 16.0f, pivot.z() / 16.0f);
		
		// B. Aplicar la traslación de la animación.
		poseStack.translate(Mth.lerp(partialTicks, previousPosX, currentPosX) / 16.0f, Mth.lerp(partialTicks, previousPosY, currentPosY) / 16.0f,
		                    Mth.lerp(partialTicks, previousPosZ, currentPosZ) / 16.0f);
		
		// C. Aplicar la escala final combinada y la rotación de la animación.
		poseStack.scale(finalScaleX, finalScaleY, animScaleZ);
		poseStack.mulPose(Axis.ZP.rotation(Mth.lerp(partialTicks, previousRotZ, currentRotZ)));
		poseStack.mulPose(Axis.YP.rotation(Mth.lerp(partialTicks, previousRotY, currentRotY)));
		poseStack.mulPose(Axis.XP.rotation(Mth.lerp(partialTicks, previousRotX, currentRotX)));
	}
}
