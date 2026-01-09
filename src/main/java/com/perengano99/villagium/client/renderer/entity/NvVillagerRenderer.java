package com.perengano99.villagium.client.renderer.entity;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.renderer.BreastModelRenderer.BreastBox;
import com.perengano99.villagium.client.renderer.HslTextureBaker;
import com.perengano99.villagium.client.renderer.SkinTones;
import com.perengano99.villagium.client.renderer.layer.NvVillagerHairClothesLayer;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import com.perengano99.villagium.entity.npc.NvVillager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class NvVillagerRenderer extends NvHumanoidRenderer<NvVillager, NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>> {
	
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/body.png");
	private static final BreastBox LEFT_BREASTBOX = new BreastBox(16, 17, -4F, 4, 5, 0.0F, false);
	private static final BreastBox RIGHT_BREASTBOX = new BreastBox(20, 17, 0F, 4, 5, 0.0F, false);
	
	private static final Identifier CLOTHES_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/clothes/default.png");
	private static final Identifier HAIR_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/hair/default.png");
	
	public NvVillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new NvVillagerModel<>(context.bakeLayer(NvVillagerModel.BODY_LAYER)));
		addLayer(new NvVillagerHairClothesLayer(this, context, false));
		addLayer(new NvVillagerHairClothesLayer(this, context, true));
	}
	
	@Override
	public NvVillagerRenderState createRenderState() {
		return new NvVillagerRenderState();
	}
	
	@Override
	public void extractRenderState(NvVillager entity, NvVillagerRenderState state, float partialTicks) {
		state.clothes = CLOTHES_TEXTURE;
		state.hair    = HAIR_TEXTURE;
		super.extractRenderState(entity, state, partialTicks);
	}
	
	@Override
	public @NonNull Identifier getTextureLocation(NvVillagerRenderState nvVillagerRenderState) {
		return HslTextureBaker.getBakedTexture(SkinTones.SKIN_TONE_IVORY, TEXTURE);
	}
	
	@Override
	protected BreastBox leftBreastBox() {
		return LEFT_BREASTBOX;
	}
	
	@Override
	protected BreastBox rightBreastBox() {
		return RIGHT_BREASTBOX;
	}
}
