package com.perengano99.villagium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.model.parts.BreastModel;
import com.perengano99.villagium.client.animations.BreastPhysicsManager;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer;
import com.perengano99.villagium.client.renderer.layer.DynamicFaceLayer;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jspecify.annotations.NonNull;

public abstract class NvHumanoidRenderer<T extends VillagiumMob<T>, S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends MobRenderer<T, S, M> {
	
	public static final BreastModelRenderer breastsRenderer = new BreastModelRenderer();
	
	public NvHumanoidRenderer(EntityRendererProvider.Context context, M model) {
		super(context, model, 0.5f);
		addLayer(new DynamicFaceLayer<>(this));
		addLayer(new BreastLayer());
	}
	
	protected abstract BreastModelRenderer.BreastBox leftBreastBox();
	
	protected abstract BreastModelRenderer.BreastBox rightBreastBox();
	
	@Override
	public void submit(S state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
	
	
	public int getTint(S state) {
		return getModelTint(state);
	}
	
	@Override
	public void extractRenderState(T entity, S state, float partialTicks) {
		state.faceModelController = BreastPhysicsManager.getFaceController(entity);
		
		state.gameTime = entity.level().getGameTime();
		
		// Pechos
		var physicsState = entity.getData(ModAttachments.BREAST_PHYSICS);
		if (physicsState instanceof BreastPhysicsState) {
			var stt = BreastPhysicsManager.get(entity);
			state.buildBreast(entity, stt, (BreastPhysicsState) physicsState, partialTicks);
		}
		super.extractRenderState(entity, state, partialTicks);
	}
	
	// Layer why LivingEntityRenderer PoseStack translations.
	private class BreastLayer extends RenderLayer<S, M> {
		
		public BreastLayer() {
			super(NvHumanoidRenderer.this);
		}
		
		@Override
		public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int light, S state, float v, float v1) {
			// Breast render
			if (state.isFemale) {
				BreastModel left = breastsRenderer.getSizedModel(leftBreastBox(), state.breastCurrentSize, state.breastCurrentZOffset);
				BreastModel right = breastsRenderer.getSizedModel(rightBreastBox(), state.breastCurrentSize, state.breastCurrentZOffset);
				
				RenderType renderType = getRenderType(state, !state.isInvisible, state.isInvisible && !state.isInvisibleToPlayer, state.appearsGlowing());
				ModelPart body = model.getBody();
				
				int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
				breastsRenderer.submitBreast(left, state, body, poseStack, BreastPhysicsState.Side.LEFT, submitNodeCollector, renderType, overlay, state.lightCoords);
				breastsRenderer.submitBreast(right, state, body, poseStack, BreastPhysicsState.Side.RIGHT, submitNodeCollector, renderType, overlay, state.lightCoords);
			}
		}
	}
}
