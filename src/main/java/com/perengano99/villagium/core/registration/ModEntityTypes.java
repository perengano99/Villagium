package com.perengano99.villagium.core.registration;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.npc.NvVillager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
	
	private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Villagium.MODID);
	
	public static final DeferredHolder<EntityType<?>, EntityType<NvVillager>> NV_VILLAGER = ENTITIES.registerEntityType("nv_villager", NvVillager::new, MobCategory.CREATURE);
	
	public static void register(IEventBus eventBus) {
		ENTITIES.register(eventBus);
	}
}
