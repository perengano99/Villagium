package com.perengano99.villagium.client.renderer.state;

import com.perengano99.villagium.client.model.BreastModel;
import com.perengano99.villagium.client.renderer.entity.VillagiumRenderer;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class NvHumanoidRenderState extends HumanoidRenderState {
	
	
	public boolean isFemale;
	public float breastCurrentSize;
	public float breastCurrentZOffset;
	
	public void buildBreast(VillagiumMob<?> creature, BreastModel.Settings settings, float partialTick){
		// Supongamos que es veija.
		
		// if (female == true)
		isFemale = true;
		breastCurrentSize = settings.getSize();
		breastCurrentZOffset = settings.getOffsetZ();
		
		VillagiumRenderer.breastsRenderer.build(settings, partialTick, creature);
	}
}
