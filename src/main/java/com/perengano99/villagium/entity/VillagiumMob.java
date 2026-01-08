package com.perengano99.villagium.entity;

import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class VillagiumMob <T extends VillagiumMob<T>> extends AgeableMob {
	private static final Logger LOGGER = Logger.getLogger();
	
	protected VillagiumMob(EntityType<? extends AgeableMob> type, Level level) {
		super(type, level);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .5).add(Attributes.FOLLOW_RANGE, 48);
	}
	
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return null;
	}
}
