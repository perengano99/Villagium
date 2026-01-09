package com.perengano99.villagium.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class NvVillagerModel<T extends NvVillagerRenderState> extends VillagiumModel<T> {
	
	public static final ModelLayerLocation BODY_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Villagium.MODID, "villager_body"), "body");
	public static final ModelLayerLocation CLOTHES_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Villagium.MODID, "villager_clothes"), "clothes");
	public static final ModelLayerLocation HAIR_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Villagium.MODID, "villager_hair"), "hair");
	
	public NvVillagerModel(ModelPart root) {
		super(root);
	}
	
	public static @NotNull LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		createLayer(partdefinition, false, true, 0, 0, 16, 16, 0, 16, 40, 16, 0, 32, 16, 32, CubeDeformation.NONE);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	
	public static @NotNull LayerDefinition createHairLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = createLayer(meshdefinition.getRoot(), false, true, 32, 16, 16, 0, 0, 0, 40, 0, 0, 16, 16, 16, new CubeDeformation(HAIR_DEFORMATION));
		createLayer(partdefinition, true, true, 32, 48, 16, 32, 0, 32, 40, 32, 0, 48, 16, 48, new CubeDeformation(HAIR_OV_DEFORMATION));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	
	public static @NotNull LayerDefinition createClothesLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = createLayer(meshdefinition.getRoot(), false, false, 0, 0, 16, 0, 0, 0, 40, 0, 0, 16, 16, 16,
		                                            new CubeDeformation(INNER_CLOTHES_DEFORMATION));
		createLayer(partdefinition, true, true, 32, 16, 16, 32, 0, 32, 40, 32, 0, 48, 16, 48, new CubeDeformation(OUTER_CLOTHES_DEFORMATION));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	
	@Contract("_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> param1")
	private static @NotNull PartDefinition createLayer(@NotNull PartDefinition partdefinition, boolean ov, boolean head, int hX, int hY, int bX, int bY, int rAX, int rAY, int lAX
			, int lAY, int rLX, int rLY, int lLX, int lLY, CubeDeformation deform) {
		
		// Cabeza (8x8 en UV, como en Minecraft)
		partdefinition.addOrReplaceChild(ov ? KEY_HEAD_OV : KEY_HEAD,
		                                 head ? CubeListBuilder.create().texOffs(hX, hY).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deform) : CubeListBuilder.create(),
		                                 PartPose.ZERO);
		
		// Cuerpo (8x12 en UV)
		PartDefinition bodyDef = partdefinition.addOrReplaceChild(ov ? KEY_BODY_OV : KEY_BODY,
		                                                          CubeListBuilder.create().texOffs(bX, bY).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deform),
		                                                          PartPose.offset(0.0F, 0.0F, 0.0F));
		
		// Brazos (4x12 en UV)
		bodyDef.addOrReplaceChild(ov ? KEY_RIGHT_ARM_OV : KEY_RIGHT_ARM, CubeListBuilder.create().texOffs(rAX, rAY).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
		                          PartPose.offset(-5.0F, 2.0F, 0.0F));
		bodyDef.addOrReplaceChild(ov ? KEY_LEFT_ARM_OV : KEY_LEFT_ARM, CubeListBuilder.create().texOffs(lAX, lAY).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
		                          PartPose.offset(5.0F, 2.0F, 0.0F));
		
		// Piernas (4x12 en UV)
		partdefinition.addOrReplaceChild(ov ? KEY_RIGHT_LEG_OV : KEY_RIGHT_LEG, CubeListBuilder.create().texOffs(rLX, rLY).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
		                                 PartPose.offsetAndRotation(-1.9F, 12.0F, 0.0F, 0, 0.01F, 0.023F));
		partdefinition.addOrReplaceChild(ov ? KEY_LEFT_LEG_OV : KEY_LEFT_LEG, CubeListBuilder.create().texOffs(lLX, lLY).mirror()
		                                                                                     .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
		                                 PartPose.offsetAndRotation(1.9F, 12.0F, 0.0F, 0, -0.01F, -0.023F));
		return partdefinition;
	}
	
	@Override
	public void translateToHand(T t, HumanoidArm humanoidArm, PoseStack poseStack) {
	
	}
}
