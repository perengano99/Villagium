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
	
	private static final Identifier F_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/body.png");
	private static final Identifier M_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/male/body.png");
	private static final BreastBox LEFT_BREASTBOX = new BreastBox(16, 17, -4F, 4, 5, 0.0F, false);
	private static final BreastBox RIGHT_BREASTBOX = new BreastBox(20, 17, 0F, 4, 5, 0.0F, false);
	
	private static final Identifier F_CLOTHES_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/clothes/default.png");
	private static final Identifier F_HAIR_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/hair/default.png");
	private static final Identifier F_FACE_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/face/default.png");
	
	private static final Identifier M_CLOTHES_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/male/clothes/default.png");
	private static final Identifier M_HAIR_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/male/hair/default.png");
	private static final Identifier M_FACE_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/male/face/default.png");
	
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
		com.perengano99.villagium.social.profile.ProfileData profile = entity.getData(com.perengano99.villagium.core.registration.ModAttachments.PROFILE_DATA.get());
		state.isFemale = profile.gender() == com.perengano99.villagium.social.profile.NvGender.FEMALE;
		
		com.perengano99.villagium.social.profile.AppearanceData app = profile.appearance();
		state.clothesTexture = com.perengano99.villagium.data.AppearanceLoader.getTexturePath(app.clothesId());
		state.hairTexture    = com.perengano99.villagium.data.AppearanceLoader.getTexturePath(app.hairId());
		state.faceTexture    = com.perengano99.villagium.data.AppearanceLoader.getTexturePath(app.faceId());
		state.skinTexture    = com.perengano99.villagium.data.AppearanceLoader.getTexturePath(app.skinId());

		com.perengano99.villagium.data.TonesLoader.ToneColorEntry tone = com.perengano99.villagium.data.TonesLoader.getColors(app.toneGroupId(), app.toneIndex());
		state.skinColor      = tone.skin();
		state.hairColor      = tone.hair();
		state.irisColor      = tone.eye();
		
		super.extractRenderState(entity, state, partialTicks);
	}
	
	@Override
	public @NonNull Identifier getTextureLocation(NvVillagerRenderState state) {
		return HslTextureBaker.getBakedTexture(state.skinColor, state.skinTexture);
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
