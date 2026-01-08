package com.perengano99.villagium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.BreastPhysicsManager;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.renderer.BreastsRenderer;
import com.perengano99.villagium.client.renderer.layer.BreastsLayer;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jspecify.annotations.NonNull;

public abstract class VillagiumRenderer<T extends VillagiumMob<T>, S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends MobRenderer<T, S, M> {
	
	public static final BreastsRenderer breastsRenderer = new BreastsRenderer();
	
	public VillagiumRenderer(EntityRendererProvider.Context context, M model) {
		super(context, model, 0.5f);
		addLayer(new BreastsLayer<>(this));
	}
	
	@Override
	public void submit(S state, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
		poseStack.pushPose();
		
		super.submit(state, poseStack, submitNodeCollector, camera);
		poseStack.popPose();
	}
	
	@Override
	public void extractRenderState(T entity, S state, float partialTicks) {
		var stt = BreastPhysicsManager.get(entity);
		stt.setSize(1.2f);
		state.buildBreast(entity, stt, partialTicks);
		
		super.extractRenderState(entity, state, partialTicks);
	}
}
