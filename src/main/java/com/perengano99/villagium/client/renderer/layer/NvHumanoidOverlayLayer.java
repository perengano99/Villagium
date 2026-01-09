package com.perengano99.villagium.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.model.parts.BreastModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer;
import com.perengano99.villagium.client.renderer.BreastModelRenderer.BreastBox;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class NvHumanoidOverlayLayer<S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends RenderLayer<S, M> {
	
	protected final M model;
	private final NvHumanoidRenderer<?, S, M> renderer;
	
	public NvHumanoidOverlayLayer(NvHumanoidRenderer<?, S, M> renderer, M model) {
		super(renderer);
		this.renderer = renderer;
		this.model    = model;
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
			if (state.isFemale) {
				ModelPart body = model.getBody();
				
				BreastModel left = NvHumanoidRenderer.breastsRenderer.getSizedModel(leftBreastBox(), state.breastCurrentSize, state.breastCurrentZOffset);
				BreastModel right = NvHumanoidRenderer.breastsRenderer.getSizedModel(rightBreastBox(), state.breastCurrentSize, state.breastCurrentZOffset);
				BreastModel leftOv = NvHumanoidRenderer.breastsRenderer.getSizedModel(leftBreastBoxOv(), state.breastCurrentSize, state.breastCurrentZOffset);
				BreastModel rightOv = NvHumanoidRenderer.breastsRenderer.getSizedModel(rightBreastBoxOv(), state.breastCurrentSize, state.breastCurrentZOffset);
				
				NvHumanoidRenderer.breastsRenderer.submitBreast(left, state, body, poseStack, BreastPhysicsState.Side.LEFT, submitNodeCollector, renderType, overlay, light);
				NvHumanoidRenderer.breastsRenderer.submitBreast(right, state, body, poseStack, BreastPhysicsState.Side.RIGHT, submitNodeCollector, renderType, overlay, light);
				NvHumanoidRenderer.breastsRenderer.submitBreast(leftOv, state, body, poseStack, BreastPhysicsState.Side.LEFT, submitNodeCollector, renderType, overlay, light);
				NvHumanoidRenderer.breastsRenderer.submitBreast(rightOv, state, body, poseStack, BreastPhysicsState.Side.RIGHT, submitNodeCollector, renderType, overlay, light);
			}
		}
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
	
	protected abstract Identifier getTextureLocation(S state);
	
	protected abstract BreastBox leftBreastBox();
	
	protected abstract BreastBox rightBreastBox();
	
	protected abstract BreastBox leftBreastBoxOv();
	
	protected abstract BreastBox rightBreastBoxOv();
}
