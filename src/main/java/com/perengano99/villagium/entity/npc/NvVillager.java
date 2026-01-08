package com.perengano99.villagium.entity.npc;

import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NvVillager extends VillagiumMob<NvVillager> {
	
	
	public NvVillager(EntityType<? extends AgeableMob> type, Level level) {
		super(type, level);
	}
}
