package com.perengano99.villagium.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.entity.ai.NvVillagerAi;
import com.perengano99.villagium.entity.interaction.MenuNpc;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.network.packets.server.OpenNpcMenuPacket;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NvVillager extends VillagiumMob<NvVillager> implements MenuNpc {
	
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
		return VillagiumMob.createAttributes().add(Attributes.TEMPT_RANGE, 35).add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FOLLOW_RANGE, 48);
	}
	
	@Override
	protected List<MemoryModuleType<?>> getMemoryTypes() {
		return MEMORY_TYPES;
	}
	
	@Override
	protected List<SensorType<? extends Sensor<? super NvVillager>>> getSensorTypes() {
		return SENSOR_TYPES;
	}
	
	protected @NonNull List<ActivityData<NvVillager>> getActivityData(NvVillager body) {
		NvVillagerAi ai = new NvVillagerAi(this);
		List<ActivityData<NvVillager>> activities = new ArrayList<>();
		activities.add(ActivityData.create(Activity.CORE, ai.getBehaviors(Activity.CORE)));
		activities.add(ActivityData.create(Activity.IDLE, ai.getBehaviors(Activity.IDLE)));
		
		return activities;
	}
	
	@Override
	protected void registerBrainActivities(Brain<NvVillager> brain) {
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		brain.updateActivityFromSchedule(level().environmentAttributes(), level().getGameTime(), position());
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		goalSelector.addGoal(2, new TemptGoal(this, 1.5f, Ingredient.of(Items.CLOCK), false));
	}
	
	@Override
	protected void customServerAiStep(@NonNull ServerLevel level) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("NvVillagerBrain");
		getBrain().tick(level, this);
		profiler.pop();
		super.customServerAiStep(level);
	}
	
	@Override
	public @NonNull InteractionResult mobInteract(Player player, @NonNull InteractionHand hand) {
		if (player.isShiftKeyDown())
			return super.mobInteract(player, hand);
		
		if (!level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
			tryOpenMenu(this, serverPlayer);
			//			if (canOpenMenu(player)) {
			//				NvProfile prof = this.getOrCreateProfile();
			//				RelationshipData rel = prof.getOrCreateRelationshipWith(player.getUUID());
			//				String tagStr = rel.getPrimaryTag().getPath();
			//
			//				//				NetworkManager.PIPELINE.sendToClient(
			//				//						serverPlayer,
			//				//						new S2C_OpenInteractionScreenPacket(
			//				//								this.getId(),
			//				//								this.getDisplayName(),
			//				//								tagStr
			//				//						)
			//				//				                                    );
			//			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public <T extends VillagiumMob<T>> void tryOpenMenu(T source, Player player) {
		if (!canOpenMenu(player))
			return;
		
		if (player instanceof ServerPlayer sp) {
			source.interactingPlayer = player;
			RelationshipData relation = source.getOrCreateProfile().getRelationWith(player.getUUID());
			RelationshipData.ClientData relationData = relation != null ? relation.toClientData() : new RelationshipData.ClientData(player.getUUID(), Map.of(), List.of(), Map.of(), Identifier.fromNamespaceAndPath(Villagium.MODID, "neutral"), null);
			NetworkManager.PIPELINE.sendToClient(sp, new OpenNpcMenuPacket(source.getId(), source.getOrCreateProfile().getData(), relationData));
		}
	}
}

