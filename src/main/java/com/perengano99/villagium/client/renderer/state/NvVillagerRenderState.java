package com.perengano99.villagium.client.renderer.state;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NvVillagerRenderState extends NvHumanoidRenderState {
	
	public Identifier clothes;
	public Identifier hair;
	
	
	// TODO: Aqui se establecera el perfil para rescatar los datos.
	public void setData() {
	
	}
}
