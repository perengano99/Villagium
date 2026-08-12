package com.perengano99.villagium.client.model.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.perengano99.villagium.client.animation.face.FaceModelAnimator;
import com.perengano99.villagium.client.model.Box;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class FacePartModel extends Box {
	
	public final Vector3f pivot;
	public final int texU, texV;
	public final boolean isIrisTinted;
	
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
		this(pivot, texU, texV, width, height, standardScaleX, standardScaleY, false);
	}
	
	public FacePartModel(Vector3f pivot, int texU, int texV, int width, int height, float standardScaleX, float standardScaleY, boolean isIrisTinted) {
		super(32, 32, texU, texV, 0, 0, 0, width, height, 0, 0, false);
		this.texU           = texU;
		this.texV           = texV;
		this.isIrisTinted   = isIrisTinted;
		baseWidth           = width;
		baseHeight          = height;
		this.pivot          = pivot;
		this.standardScaleX = standardScaleX;
		this.standardScaleY = standardScaleY;
		
		this.currentScaleX = this.previousScaleX = 1;
		this.currentScaleY = this.previousScaleY = 1;
		this.currentScaleZ = this.previousScaleZ = 1;
	}
	
	public float getAnimPosX(float partialTicks) {
		return Mth.lerp(partialTicks, previousPosX, currentPosX);
	}
	
	public float getAnimPosY(float partialTicks) {
		return Mth.lerp(partialTicks, previousPosY, currentPosY);
	}
	
	public float getAnimScaleX(float partialTicks) {
		return standardScaleX * Mth.lerp(partialTicks, previousScaleX, currentScaleX);
	}
	
	public float getAnimScaleY(float partialTicks) {
		return standardScaleY * Mth.lerp(partialTicks, previousScaleY, currentScaleY);
	}
	
	public void commitTransforms(FaceModelAnimator.AnimationTargets targets) {
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
	
	public float getAnimPosZ(float partialTicks) {
		return Mth.lerp(partialTicks, previousPosZ, currentPosZ);
	}
	
	public float getAnimRotX(float partialTicks) {
		return Mth.lerp(partialTicks, previousRotX, currentRotX);
	}
	
	public float getAnimRotY(float partialTicks) {
		return Mth.lerp(partialTicks, previousRotY, currentRotY);
	}
	
	public float getAnimRotZ(float partialTicks) {
		return Mth.lerp(partialTicks, previousRotZ, currentRotZ);
	}
	
	public float getAnimScaleZ(float partialTicks) {
		return Mth.lerp(partialTicks, previousScaleZ, currentScaleZ);
	}
	
	public void applyGuiTransformations(org.joml.Matrix3x2fStack poseStack, float partialTicks) {
		float finalScaleX = getAnimScaleX(partialTicks);
		float finalScaleY = getAnimScaleY(partialTicks);
		
		float pivotOffsetX = baseWidth / 2.0f;
		float pivotOffsetY = baseHeight / 2.0f;
		
		// 1. Base pivot + anim position
		poseStack.translate(pivot.x() + getAnimPosX(partialTicks),
		                    pivot.y() + getAnimPosY(partialTicks));
		
		// 2. Rotation around local part center (2D rotation)
		poseStack.translate(pivotOffsetX, pivotOffsetY);
		float rotZ = getAnimRotZ(partialTicks);
		if (rotZ != 0.0f) poseStack.rotate(rotZ);
		poseStack.translate(-pivotOffsetX, -pivotOffsetY);
		
		// 3. Combined scale
		poseStack.scale(finalScaleX, finalScaleY);
	}
	
	public void applyTransformations(PoseStack poseStack, float partialTicks) {
		float animScaleX = Mth.lerp(partialTicks, previousScaleX, currentScaleX);
		float animScaleY = Mth.lerp(partialTicks, previousScaleY, currentScaleY);
		float animScaleZ = Mth.lerp(partialTicks, previousScaleZ, currentScaleZ);
		float finalScaleX = this.standardScaleX * animScaleX;
		float finalScaleY = this.standardScaleY * animScaleY;
		
		float pivotOffsetX = baseWidth / 2.0f;
		float pivotOffsetY = baseHeight / 2.0f;
		
		// 1. Pivot base + anim translation
		poseStack.translate(pivot.x() / 16.0f, pivot.y() / 16.0f, pivot.z() / 16.0f);
		poseStack.translate(Mth.lerp(partialTicks, previousPosX, currentPosX) / 16.0f,
		                    Mth.lerp(partialTicks, previousPosY, currentPosY) / 16.0f,
		                    Mth.lerp(partialTicks, previousPosZ, currentPosZ) / 16.0f);
		
		// 2. Rotation around local part center (before scaling)
		poseStack.translate(pivotOffsetX / 16.0f, pivotOffsetY / 16.0f, 0.0f);
		poseStack.mulPose(Axis.ZP.rotation(Mth.lerp(partialTicks, previousRotZ, currentRotZ)));
		poseStack.mulPose(Axis.YP.rotation(Mth.lerp(partialTicks, previousRotY, currentRotY)));
		poseStack.mulPose(Axis.XP.rotation(Mth.lerp(partialTicks, previousRotX, currentRotX)));
		poseStack.translate(-pivotOffsetX / 16.0f, -pivotOffsetY / 16.0f, 0.0f);
		
		// 3. Combined scale
		poseStack.scale(finalScaleX, finalScaleY, animScaleZ);
	}
}
