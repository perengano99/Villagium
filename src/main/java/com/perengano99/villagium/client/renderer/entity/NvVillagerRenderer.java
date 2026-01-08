package com.perengano99.villagium.client.renderer.entity;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.renderer.state.NvVillagerRenderState;
import com.perengano99.villagium.entity.npc.NvVillager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class NvVillagerRenderer extends VillagiumRenderer <NvVillager, NvVillagerRenderState, NvVillagerModel<NvVillagerRenderState>>{
	
	public NvVillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new NvVillagerModel<>(context.bakeLayer(NvVillagerModel.BODY_LAYER)));
	}
	
	@Override
	public NvVillagerRenderState createRenderState() {
		return new NvVillagerRenderState();
	}
	
	@Override
	public @NonNull Identifier getTextureLocation(NvVillagerRenderState nvVillagerRenderState) {
		return Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/entity/nv_villager/female/body.png");
	}
}
