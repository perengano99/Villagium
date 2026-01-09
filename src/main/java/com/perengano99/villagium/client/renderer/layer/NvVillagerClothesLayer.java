package com.perengano99.villagium.client.renderer.layer;

import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer.BreastBox;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class NvVillagerClothesLayer extends NvHumanoidOverlayLayer<NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> {
	
	private static final BreastBox LEFT_BREASTBOX = new BreastBox(16, 1, -4F, 4, 5, NvVillagerModel.INNER_CLOTHES_DEFORMATION, false);
	private static final BreastBox RIGHT_BREASTBOX = new BreastBox(20, 1, 0F, 4, 5, NvVillagerModel.INNER_CLOTHES_DEFORMATION, false);
	private static final BreastBox LEFT_BREASTBOX_OV = new BreastBox(16, 33, -4F, 4, 5, NvVillagerModel.OUTER_CLOTHES_DEFORMATION, false);
	private static final BreastBox RIGHT_BREASTBOX_OV = new BreastBox(20, 33, 0F, 4, 5, NvVillagerModel.OUTER_CLOTHES_DEFORMATION, false);
	
	public NvVillagerClothesLayer(NvHumanoidRenderer<?, NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> renderer, EntityRendererProvider.Context context) {
		super(renderer, new NvVillagerModel<>(context.bakeLayer(NvVillagerModel.CLOTHES_LAYER)));
	}
	
	@Override
	protected Identifier getTextureLocation(NvVillagerRenderState state) {
		return state.clothes;
	}
	
	@Override
	protected BreastBox leftBreastBox() {
		return LEFT_BREASTBOX;
	}
	
	@Override
	protected BreastBox rightBreastBox() {
		return RIGHT_BREASTBOX;
	}
	
	@Override
	protected BreastBox leftBreastBoxOv() {
		return LEFT_BREASTBOX_OV;
	}
	
	@Override
	protected BreastBox rightBreastBoxOv() {
		return RIGHT_BREASTBOX_OV;
	}
}
