package com.perengano99.villagium.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.model.parts.BreastModel;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer;
import com.perengano99.villagium.client.renderer.entity.VillagiumRenderer;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
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
import org.jspecify.annotations.Nullable;

public class BreastLayer<S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends RenderLayer<S, M> {
	private static final BreastModelRenderer.BreastBox LEFT_REQUEST = new BreastModelRenderer.BreastBox(16, 17, -4F, 4, 5, 0.0F, false);
	private static final BreastModelRenderer.BreastBox RIGHT_REQUEST = new BreastModelRenderer.BreastBox(20, 17, 0F, 4, 5, 0.0F, false);
	
	private LivingEntityRenderer parentRenderer;
	
	public BreastLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
		parentRenderer = (LivingEntityRenderer) renderer;
	}
	
	@Override
	public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packetLight, S state, float v, float v1) {
		if (!state.isFemale)
			return;
		
		BreastModel left = VillagiumRenderer.breastsRenderer.getSizedModel(LEFT_REQUEST, state.breastCurrentSize, state.breastCurrentZOffset);
		BreastModel right = VillagiumRenderer.breastsRenderer.getSizedModel(RIGHT_REQUEST, state.breastCurrentSize, state.breastCurrentZOffset);
		
		RenderType renderType = getRenderType(state, !state.isInvisible, state.appearsGlowing());
		ModelPart body = getParentModel().getBody();
		
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
		VillagiumRenderer.breastsRenderer.submitBreast(left, state, body, poseStack, BreastPhysicsState.Side.LEFT, submitNodeCollector, renderType, overlay, packetLight);
		VillagiumRenderer.breastsRenderer.submitBreast(right, state, body, poseStack, BreastPhysicsState.Side.RIGHT, submitNodeCollector, renderType, overlay, packetLight);
	}
	
	protected @Nullable RenderType getRenderType(S state, boolean isBodyVisible, boolean appearGlowing) {
		Identifier texture = parentRenderer.getTextureLocation(state);
		if (isBodyVisible)
			return this.getParentModel().renderType(texture);
		else
			return appearGlowing ? RenderTypes.outline(texture) : null;
	}
}
