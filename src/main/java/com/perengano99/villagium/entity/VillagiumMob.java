package com.perengano99.villagium.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public abstract class VillagiumMob<T extends VillagiumMob<T>> extends AgeableMob {
	
	private static final Logger LOGGER = Logger.getLogger();
	
	protected VillagiumMob(EntityType<? extends AgeableMob> type, Level level) {
		super(type, level);
	}
	
	
	@Override
	public @NonNull Brain<T> getBrain() {
		return (Brain<T>) super.getBrain();
	}
	
	@Override
	protected Brain.@NonNull Provider<?> brainProvider() {
		return Brain.provider(getMemoryTypes(), getSensorTypes());
	}
	
	@Override
	protected @NonNull Brain<?> makeBrain(@NonNull Dynamic<?> input) {
		Brain<T> brain = (Brain<T>) brainProvider().makeBrain(input);
		registerBrainActivities(brain);
		return brain;
	}
	
	public final void rebuildBrain() {
		NbtOps nbtops = NbtOps.INSTANCE;
		this.brain = this.makeBrain(new Dynamic<>(nbtops, (Tag) nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), (Tag) nbtops.emptyMap()))));
	}
	
	protected abstract Collection<MemoryModuleType<?>> getMemoryTypes();
	
	protected abstract Collection<SensorType<? extends Sensor<? super T>>> getSensorTypes();
	
	protected abstract void registerBrainActivities(Brain<T> brain);
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE).add(Attributes.ATTACK_SPEED).add(Attributes.BLOCK_INTERACTION_RANGE, 1.5f).add(Attributes.BLOCK_BREAK_SPEED);
	}
	
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return null;
	}
}
