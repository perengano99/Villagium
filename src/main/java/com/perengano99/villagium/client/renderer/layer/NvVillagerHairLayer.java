package com.perengano99.villagium.client.renderer.layer;

import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class NvVillagerHairLayer extends NvHumanoidOverlayLayer<NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> {
	
	private static final BreastModelRenderer.BreastBox LEFT_BREASTBOX = new BreastModelRenderer.BreastBox(16, 1, -4F, 4, 5, NvVillagerModel.HAIR_DEFORMATION, false);
	private static final BreastModelRenderer.BreastBox RIGHT_BREASTBOX = new BreastModelRenderer.BreastBox(20, 1, 0F, 4, 5, NvVillagerModel.HAIR_DEFORMATION, false);
	private static final BreastModelRenderer.BreastBox LEFT_BREASTBOX_OV = new BreastModelRenderer.BreastBox(16, 33, -4F, 4, 5, NvVillagerModel.HAIR_OV_DEFORMATION, false);
	private static final BreastModelRenderer.BreastBox RIGHT_BREASTBOX_OV = new BreastModelRenderer.BreastBox(20, 33, 0F, 4, 5, NvVillagerModel.HAIR_OV_DEFORMATION, false);
	
	public NvVillagerHairLayer(NvHumanoidRenderer<?, NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> renderer, EntityRendererProvider.Context context) {
		super(renderer, new NvVillagerModel<>(context.bakeLayer(NvVillagerModel.HAIR_LAYER)));
	}
	
	@Override
	protected Identifier getTextureLocation(NvVillagerRenderState state) {
		//TODO: Se le aplicara hsl color a esta textura...
		return state.hair;
	}
	
	@Override
	protected BreastModelRenderer.BreastBox leftBreastBox() {
		return LEFT_BREASTBOX;
	}
	
	@Override
	protected BreastModelRenderer.BreastBox rightBreastBox() {
		return RIGHT_BREASTBOX;
	}
	
	@Override
	protected BreastModelRenderer.BreastBox leftBreastBoxOv() {
		return LEFT_BREASTBOX_OV;
	}
	
	@Override
	protected BreastModelRenderer.BreastBox rightBreastBoxOv() {
		return RIGHT_BREASTBOX_OV;
	}
}
