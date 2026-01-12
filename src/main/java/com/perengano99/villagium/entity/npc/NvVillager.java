package com.perengano99.villagium.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.entity.ai.NvVillagerAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

public class NvVillager extends VillagiumMob<NvVillager> {
	
	
	private static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
			MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET,
			MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
			MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	
	private static final List<SensorType<? extends Sensor<? super NvVillager>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
	
	public NvVillager(EntityType<? extends AgeableMob> type, Level level) {
		super(type, level);
		getNavigation().setCanOpenDoors(true);
		getNavigation().setCanFloat(true);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return VillagiumMob.createAttributes().add(Attributes.TEMPT_RANGE, 35).add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .5).add(Attributes.FOLLOW_RANGE, 48);
	}
	
	@Override
	protected Collection<MemoryModuleType<?>> getMemoryTypes() {
		return MEMORY_TYPES;
	}
	
	@Override
	protected Collection<SensorType<? extends Sensor<? super NvVillager>>> getSensorTypes() {
		return SENSOR_TYPES;
	}
	
	@Override
	protected void registerBrainActivities(Brain<NvVillager> brain) {
		NvVillagerAi ai = new NvVillagerAi(this);
		brain.addActivity(Activity.CORE, ai.getBehaviors(Activity.CORE));
		brain.addActivity(Activity.IDLE, ai.getBehaviors(Activity.IDLE));
		
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		brain.updateActivityFromSchedule(level().environmentAttributes(), level().getGameTime(), position());
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		goalSelector.addGoal(2, new TemptGoal(this, 0.65f, Ingredient.of(Items.CLOCK), false));
	}
	
	@Override
	protected void customServerAiStep(@NonNull ServerLevel level) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("NvVillagerBrain");
		getBrain().tick(level, this);
		profiler.pop();
		super.customServerAiStep(level);
	}
}
