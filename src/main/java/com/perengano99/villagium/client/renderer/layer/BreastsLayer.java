package com.perengano99.villagium.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.client.model.BreastModel;
import com.perengano99.villagium.client.model.VillagiumModel;
import com.perengano99.villagium.client.renderer.BreastsRenderer;
import com.perengano99.villagium.client.renderer.entity.VillagiumRenderer;
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

public class BreastsLayer<S extends NvHumanoidRenderState, M extends VillagiumModel<S>> extends RenderLayer<S, M> {
	
	private static final BreastsRenderer renderer = new BreastsRenderer();
	private static final BreastModel.Settings settings = new BreastModel.Settings(null);
	
	private static final BreastsRenderer.BreastBox LEFT_REQUEST = new BreastsRenderer.BreastBox(16, 17, -4F, 4, 5, 0.0F, false);
	private static final BreastsRenderer.BreastBox RIGHT_REQUEST = new BreastsRenderer.BreastBox(20, 17, 0F, 4, 5, 0.0F, false);
	
	private LivingEntityRenderer parentRenderer;
	
	public BreastsLayer(RenderLayerParent<S, M> renderer) {
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
		
		VillagiumRenderer.breastsRenderer.submitBreast(left, state, body, poseStack, BreastsRenderer.Side.LEFT, submitNodeCollector, renderType, packetLight);
		VillagiumRenderer.breastsRenderer.submitBreast(right, state, body, poseStack, BreastsRenderer.Side.RIGHT, submitNodeCollector, renderType, packetLight);
	}
	
	protected @Nullable RenderType getRenderType(S state, boolean isBodyVisible, boolean appearGlowing) {
		Identifier texture = parentRenderer.getTextureLocation(state);
		if (isBodyVisible)
			return this.getParentModel().renderType(texture);
		else
			return appearGlowing ? RenderTypes.outline(texture) : null;
	}
}
