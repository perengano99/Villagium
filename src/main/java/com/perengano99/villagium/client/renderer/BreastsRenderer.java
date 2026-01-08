package com.perengano99.villagium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.perengano99.villagium.client.model.Box;
import com.perengano99.villagium.client.model.BreastModel;
import com.perengano99.villagium.client.model.BreastModelPhyisics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import org.joml.*;
import org.jspecify.annotations.NonNull;

import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

public class BreastsRenderer {
	
	public enum Side { LEFT, RIGHT }
	
	public record BreastBox(int texU, int texV, float x, int dx, int dy, float delta, boolean mirror) {}
	
	private static final Map<CacheKey, BreastModel> modelCache = new HashMap<>();
	
	private record CacheKey(BreastBox box, float size, float zOffset) {}
	
	public BreastModel getSizedModel(BreastBox box, float size, float zOffset) {
		CacheKey key = new CacheKey(box, size, zOffset);
		return modelCache.computeIfAbsent(key, k -> BreastModel.createSized(64, 64, k.box.texU, k.box.texV, k.box.x, 0, 0, k.box.dx, k.box.dy, k.box.delta, k.box.mirror, k.size,
		                                                                    k.zOffset));
	}
	
	private float offsetX, offsetY, offsetZ,
			lPhysPositionY, lPhysPositionX,
			rPhysPositionY, rPhysPositionX,
			lPhysBounceRotation, rPhysBounceRotation,
			size, zOffset, outwardAngle;
	
	private boolean breathingAnimation;
	
	public <S extends HumanoidRenderState> void build(BreastModel.Settings settings, float delta, LivingEntity entity) {
		offsetX = settings.getOffsetX();
		offsetY = settings.getOffsetY();
		offsetZ = settings.getOffsetZ();
		
		BreastModelPhyisics lPhysics = settings.getLeftPhysics();
		BreastModelPhyisics rPhysics = settings.getRightPhysics();
		
		final float bSize = settings.getSize();
		outwardAngle = settings.getOutward();
		
		// Left
		lPhysPositionY      = lPhysics.getPositionY(delta);
		lPhysPositionX      = lPhysics.getPositionX(delta);
		lPhysBounceRotation = lPhysics.getBounceRotation(delta);
		
		// Right
		rPhysPositionY      = rPhysics.getPositionY(delta);
		rPhysPositionX      = rPhysics.getPositionX(delta);
		rPhysBounceRotation = rPhysics.getBounceRotation(delta);
		
		if (bSize > 0.7f) size = bSize;
		else size = Math.min(bSize * 1.5f, 0.7f);
		
		zOffset = 0.0625f - (bSize * 0.0625f);
		size *= 0.5f * Math.abs(bSize - 0.7f) * 2;
		
		breathingAnimation = (!entity.isUnderWater() || entity.hasEffect(MobEffects.WATER_BREATHING) || entity.level().getBlockState(
				new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ())).is(Blocks.BUBBLE_COLUMN));
	}
	
	public <S extends HumanoidRenderState> void submitBreast(BreastModel model, S state, ModelPart body, PoseStack poseStack, Side side, SubmitNodeCollector submitNodeCollector,
	                                                         RenderType renderType, int light) {
		poseStack.pushPose();
		setupTransforms(state, body, poseStack, side);
		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
			renderBox(model, side, pose, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFF);
		});
		poseStack.popPose();
	}
	
	public <S extends HumanoidRenderState> void setupTransforms(@NonNull S state, ModelPart body, PoseStack poseStack, Side side) {
		if (state.isBaby) {
			poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
			poseStack.translate(0, 0.75f, 0);
		}
		
		body.translateAndRotate(poseStack);
		poseStack.translate((side == Side.LEFT ? lPhysPositionX : rPhysPositionX) / 32f, 0, 0);
		poseStack.translate(0, (side == Side.LEFT ? lPhysPositionY : rPhysPositionY) / 32f, 0);
		
		// Ajuste de posición base (Offset)
		float sideSign = (side == Side.LEFT ? 1 : -1);
		
		poseStack.translate(
				(sideSign * offsetX) * 0.0625f,
				0.05625f + (offsetY * 0.0625f),
				zOffset - 0.125f + (offsetZ * 0.0625f)); // zOffset - 0.0625f * 2f
		
		// Rotación del rebote (Physics Bounce)
		poseStack.translate(-0.125f * sideSign, 0, 0); // -0.0625f * 2
		
		float bounceRotRad = (float) Math.toRadians(side == Side.LEFT ? lPhysBounceRotation : rPhysBounceRotation);
		poseStack.mulPose(new Quaternionf().rotationXYZ(0, bounceRotRad, 0));
		
		poseStack.translate(0.125f * sideSign, 0, 0); // +0.0625f * 2
		
		float rotation = size;
		poseStack.translate(0, -0.035f * size, 0);
		rotation -= (side == Side.LEFT ? lPhysPositionY : rPhysPositionY) / 12f;
		rotation = Math.min(rotation, size + 0.2f);
		rotation = Math.min(rotation, 1);
		
		float baseAngle = -35f;
		Quaternionf rotationTransform = new Quaternionf()
				.rotationY((side == Side.LEFT ? outwardAngle : -outwardAngle) * Mth.DEG_TO_RAD)
				.rotateX((baseAngle + (rotation * 10f)) * Mth.DEG_TO_RAD);
		
		if (breathingAnimation) {
			float f5 = -Mth.cos(state.ageInTicks * 0.09F) * 0.45F + 0.45F;
			rotationTransform.rotateX(f5 * Mth.DEG_TO_RAD);
		}
		
		poseStack.mulPose(rotationTransform);
		poseStack.scale(0.9995f, 1f, 1f);
	}
	
	private static void renderBox(Box model, Side side, PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		for (var quad : model.quads) {
			if (quad == null) continue; // Evita el error para las caras invisibles.
			if (side == Side.LEFT && quad.normal.x > 0) continue;
			if (side == Side.RIGHT && quad.normal.x < 0) continue;
			
			Vector3f vector3f = new Vector3f(quad.normal.x, quad.normal.y, quad.normal.z).mul(matrix3f);
			float normalX = vector3f.x;
			float normalY = vector3f.y;
			float normalZ = vector3f.z;
			for (var vertex : quad.vertexPositions) {
				float j = vertex.x() / 16.0F;
				float k = vertex.y() / 16.0F;
				float l = vertex.z() / 16.0F;
				Vector4f vector4f = new Vector4f(j, k, l, 1.0F).mul(matrix4f);
				vertexConsumer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), color, vertex.u(), vertex.v(), overlay, light, normalX, normalY, normalZ);
			}
		}
	}
}
