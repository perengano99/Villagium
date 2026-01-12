package com.perengano99.villagium.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.animation.face.FaceModelAnimator;
import com.perengano99.villagium.client.model.NvHumanoidModel;
import com.perengano99.villagium.client.model.parts.FacePartModel;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class DynamicFaceLayer<S extends NvHumanoidRenderState, M extends NvHumanoidModel<S>> extends RenderLayer<S, M> {
	
	public DynamicFaceLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}
	
	@Override
	public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int light, S state, float v, float v1) {
		if (state.faceModelController == null)
			return;
		
		FaceModelAnimator<S> controller = (FaceModelAnimator<S>) state.faceModelController;
		controller.update(state);
		
		ModelPart head = getParentModel().getHead();
		
		poseStack.pushPose();
		head.translateAndRotate(poseStack);
		// Colocamos el origen en el centro del frontal de la cara.
		poseStack.translate(0.0F, -0.25F, -0.250625F);
		
		boolean forceTransparent = state.isInvisible && !state.isInvisibleToPlayer;
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
		RenderType renderType = getRenderType(state, forceTransparent);
		
		for (FacePartModel part : controller.getAllParts()) {
			int color = 0xFFFFFF;
			if (part == controller.leftIris || part == controller.rightIris)
				color = state.irisColor;
			render(part, poseStack, submitNodeCollector, renderType, light, overlay, color, state.partialTick);
		}
		poseStack.popPose();
	}
	
	private void render(FacePartModel model, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int packedLight, int packedOverlay, int color,
	                    float partialTicks) {
		poseStack.pushPose();
		model.applyTransformations(poseStack, partialTicks);
		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
			model.render(pose, vertexConsumer, packedLight, packedOverlay, color);
		});
		poseStack.popPose();
	}
	
	private RenderType getRenderType(S state, boolean forceTransparent) {
		Identifier texture = state.faceTexture;
		if (forceTransparent)
			return RenderTypes.itemEntityTranslucentCull(texture);
		return RenderTypes.entityCutoutNoCull(texture);
	}
}
