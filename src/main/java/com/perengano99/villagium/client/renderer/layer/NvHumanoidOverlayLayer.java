package com.perengano99.villagium.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.model.parts.BreastModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer.BreastBox;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class NvHumanoidOverlayLayer<S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends RenderLayer<S, M> {
	
	protected final M model;
	@Nullable
	protected BreastConfig breastConfig;
	private final NvHumanoidRenderer<?, S, M> renderer;
	
	protected NvHumanoidOverlayLayer(NvHumanoidRenderer<?, S, M> renderer, M model, @Nullable BreastConfig breastConfig) {
		super(renderer);
		this.renderer     = renderer;
		this.model        = model;
		this.breastConfig = breastConfig;
	}
	
	@Override
	public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int light, S state, float v, float v1) {
		RenderType renderType = getRenderType(state, !state.isInvisible, state.isInvisible && !state.isInvisibleToPlayer, state.appearsGlowing());
		
		// Animación?..
		if (renderType != null) {
			int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
			boolean isBodyVisible = !state.isInvisible;
			boolean forceTransparent = !isBodyVisible && !state.isInvisibleToPlayer;
			int baseColor = forceTransparent ? 654311423 : -1;
			int tintedColor = ARGB.multiply(baseColor, renderer.getTint(state));
			
			submitNodeCollector.submitModel(model, state, poseStack, renderType, light, overlay, tintedColor, null, state.outlineColor, null);
			
			// Breast render
			if (breastConfig != null && shouldRenderBreast(state)) {
				ModelPart body = model.getBody();
				
				renderBreast(breastConfig.left, state, poseStack, BreastPhysicsState.Side.LEFT, body, submitNodeCollector, renderType, overlay, light);
				renderBreast(breastConfig.right, state, poseStack, BreastPhysicsState.Side.RIGHT, body, submitNodeCollector, renderType, overlay, light);
				renderBreast(breastConfig.leftOv, state, poseStack, BreastPhysicsState.Side.LEFT, body, submitNodeCollector, renderType, overlay, light);
				renderBreast(breastConfig.rightOv, state, poseStack, BreastPhysicsState.Side.RIGHT, body, submitNodeCollector, renderType, overlay, light);
			}
		}
	}
	
	private void renderBreast(BreastBox box, S state, PoseStack poseStack, BreastPhysicsState.Side side, ModelPart body, SubmitNodeCollector submitNodeCollector,
	                          RenderType renderType, int overlay, int light) {
		if (box == null)
			return;
		
		BreastModel model = NvHumanoidRenderer.breastsRenderer.getSizedModel(box, state.breastCurrentSize, state.breastCurrentZOffset);
		NvHumanoidRenderer.breastsRenderer.submitBreast(model, state, body, poseStack, side, submitNodeCollector, renderType, overlay, light);
	}
	
	protected @Nullable RenderType getRenderType(S state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		Identifier texture = getTextureLocation(state);
		if (forceTransparent)
			return RenderTypes.itemEntityTranslucentCull(texture);
		else if (isBodyVisible)
			return this.model.renderType(texture);
		else
			return appearGlowing ? RenderTypes.outline(texture) : null;
	}
	
	protected abstract boolean shouldRenderBreast(S state);
	
	protected abstract Identifier getTextureLocation(S state);
	
	protected record BreastConfig(BreastBox left, BreastBox right, BreastBox leftOv, BreastBox rightOv) {}
}
