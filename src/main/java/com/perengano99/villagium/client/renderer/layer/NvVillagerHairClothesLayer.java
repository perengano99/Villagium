package com.perengano99.villagium.client.renderer.layer;

import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer.BreastBox;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class NvVillagerHairClothesLayer extends NvHumanoidOverlayLayer<NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> {
	
	private final boolean isHair;
	
	public NvVillagerHairClothesLayer(NvHumanoidRenderer renderer, EntityRendererProvider.Context context, boolean hair) {
		super(renderer, new NvVillagerModel<>(context.bakeLayer(hair ? NvVillagerModel.HAIR_LAYER : NvVillagerModel.CLOTHES_LAYER)), getBreastConfig(hair));
		isHair = hair;
	}
	
	@Override
	protected boolean shouldRenderBreast(NvVillagerRenderState state) {
		return state.isFemale;
	}
	
	@Override
	protected Identifier getTextureLocation(NvVillagerRenderState state) {
		return isHair ? state.hair : state.clothes;
	}
	
	private static BreastConfig getBreastConfig(boolean hair) {
		int texV1 = hair ? 33 : 1;
		int texV2 = hair ? 33 : 1;
		float d1 = hair ? NvVillagerModel.HAIR_DEFORMATION : NvVillagerModel.INNER_CLOTHES_DEFORMATION;
		float d2 = hair ? NvVillagerModel.HAIR_OV_DEFORMATION : NvVillagerModel.OUTER_CLOTHES_DEFORMATION;
		
		return new BreastConfig(
				new BreastBox(16, texV1, -4, 4, 5, d1, false),
				new BreastBox(20, texV1, 0, 4, 5, d1, false),
				new BreastBox(16, texV2, -4, 4, 5, d2, false),
				new BreastBox(20, texV2, 0, 4, 5, d2, false)
		);
	}
}
