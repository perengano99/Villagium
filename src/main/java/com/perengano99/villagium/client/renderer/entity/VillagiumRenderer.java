package com.perengano99.villagium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.physics.BreastPhysicsManager;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer;
import com.perengano99.villagium.client.renderer.layer.BreastLayer;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jspecify.annotations.NonNull;

public abstract class VillagiumRenderer<T extends VillagiumMob<T>, S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends MobRenderer<T, S, M> {
	
	public static final BreastModelRenderer breastsRenderer = new BreastModelRenderer();
	
	public VillagiumRenderer(EntityRendererProvider.Context context, M model) {
		super(context, model, 0.5f);
		addLayer(new BreastLayer<>(this));
	}
	
	@Override
	public void submit(S state, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
		poseStack.pushPose();
		
		super.submit(state, poseStack, submitNodeCollector, camera);
		poseStack.popPose();
	}
	
	@Override
	public void extractRenderState(T entity, S state, float partialTicks) {
		
		var physicsState = entity.getData(ModAttachments.BREAST_PHYSICS);
		if (physicsState instanceof BreastPhysicsState) {
			var stt = BreastPhysicsManager.get(entity);
			state.buildBreast(entity, stt, (BreastPhysicsState) physicsState, partialTicks);
		}
		super.extractRenderState(entity, state, partialTicks);
	}
}
