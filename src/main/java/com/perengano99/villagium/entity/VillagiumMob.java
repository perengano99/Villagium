package com.perengano99.villagium.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.SyncAppearanceToClientPacket;
import com.perengano99.villagium.social.profile.ProfileData;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.social.profile.NvGender;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.profile.NvProfileFactory;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public abstract class VillagiumMob<T extends VillagiumMob<T>> extends AgeableMob {
	
	private static final net.minecraft.network.syncher.EntityDataAccessor<String> BIRTH_TIME =
			net.minecraft.network.syncher.SynchedEntityData.defineId(VillagiumMob.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
	
	private static final Logger LOGGER = Logger.getLogger();
	protected Brain.Provider<T> BRAIN_PROVIDER;
	private NvProfile profile;
	@Nullable public Player interactingPlayer;
	private boolean isReadyToInitializeProfile = false;
	
	protected VillagiumMob(EntityType<? extends AgeableMob> type, Level level) {
		super(type, level);
		if (!level.isClientSide())
			setCustomNameVisible(true);
	}
	
	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(BIRTH_TIME, "");
	}
	
	public String getBirthTime() {
		return this.entityData.get(BIRTH_TIME);
	}
	
	public void setBirthTime(String birthTime) {
		this.entityData.set(BIRTH_TIME, birthTime);
	}
	
	@Override
	public @NonNull Brain<T> getBrain() {
		return (Brain<T>) super.getBrain();
	}
	
	protected @NonNull Brain<T> makeBrain(Brain.@NonNull Packed packedBrain) {
		if (BRAIN_PROVIDER == null)
			BRAIN_PROVIDER = Brain.provider(getSensorTypes(), this::getActivityData);
		
		Brain<T> brain = (Brain<T>) BRAIN_PROVIDER.makeBrain((T) this, packedBrain);
		registerBrainActivities(brain);
		return brain;
	}
	
	public void refreshBrain(ServerLevel level) {
		Brain<T> oldBrain = this.getBrain();
		oldBrain.stopAll(level, (T) this);
		this.brain = BRAIN_PROVIDER.makeBrain((T) this, oldBrain.pack());
		registerBrainActivities(getBrain());
	}
	
	protected abstract List<MemoryModuleType<?>> getMemoryTypes();
	
	protected abstract List<ActivityData<T>> getActivityData(T body);
	
	protected abstract List<SensorType<? extends Sensor<? super T>>> getSensorTypes();
	
	protected abstract void registerBrainActivities(Brain<T> brain);
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE).add(Attributes.ATTACK_SPEED).add(Attributes.BLOCK_INTERACTION_RANGE, 1.5f).add(Attributes.BLOCK_BREAK_SPEED);
	}
	
	public final AnimationState idleAnimState = new AnimationState();
	public final AnimationState triggerAnimState = new AnimationState();
	private String activeTriggerId;
	private int idleAnimationTimeout = 0;
	private static final int IDLE_ANIMATION_TICKS = (int) (3.5f * 20);
	
	public String getActiveTriggerId() {
		return this.activeTriggerId;
	}
	
	public void triggerAnimation(String animId) {
		this.activeTriggerId = animId;
		if (animId != null) {
			this.triggerAnimState.start(this.tickCount);
		} else {
			this.triggerAnimState.stop();
		}
	}
	
	public boolean isPanicking() {
		return this.getLastHurtByMob() != null && this.getLastHurtByMobTimestamp() > this.tickCount - 100;
	}
	
	public boolean isRunning() {
		return this.walkAnimation.speed() >= 0.8F;
	}
	
	private int manualAnimDuration = -1;
	
	public void setManualAnimDuration(int ticks) {
		this.manualAnimDuration = ticks;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!level().isClientSide()) {
			if (manualAnimDuration > 0) {
				manualAnimDuration--;
				if (manualAnimDuration == 0) {
					if (entityTags().contains("animation_debug_frozen")) {
						setNoAi(false);
						removeTag("animation_debug_frozen");
					}
					manualAnimDuration = -1;
				}
			}
		} else
			setupAnimationStates();
	}
	
	private void setupAnimationStates() {
		if (this.idleAnimationTimeout <= 0) {
			idleAnimationTimeout = IDLE_ANIMATION_TICKS;
			idleAnimState.start(tickCount);
		} else
			--idleAnimationTimeout;
	}
	
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return null;
	}
	
	public NvProfile getOrCreateProfile() {
		if (level().isClientSide())
			throw new IllegalStateException("Cannot access full NvProfile on the client side");
		if (profile == null)
			profile = new NvProfile(this);
		
		if (!isReadyToInitializeProfile)
			return profile;
		
		ProfileData current = profile.getData();
		ProfileData defs = ProfileData.defaults();
		boolean isIncomplete = !current.appearance().isGenerated()
		                       || current.gender() == NvGender.UNDEFINED
		                       || current.displayName().getString().equals(defs.displayName().getString())
		                       || current.personalityId().equals(ProfileData.UNSPECIFIED_PERSONALITY)
		                       || current.cultureId().equals(ProfileData.UNSPECIFIED_CULTURE)
		                       || current.traits().contains(ProfileData.UNSPECIFIED_TRAIT);
		
		if (isIncomplete)
			NvProfileFactory.generateNewProfile(profile);
		
		return profile;
	}
	
	@Override
	public @NonNull Component getDisplayName() {
		return getData(ModAttachments.PROFILE_DATA.get()).displayName();
	}
	
	public void syncAppearanceToTracking() {
		if (!level().isClientSide()) {
			ProfileData data = getData(ModAttachments.PROFILE_DATA.get());
			if (data.appearance().isGenerated())
				NetworkManager.PIPELINE.sendToTracking(this, new SyncAppearanceToClientPacket(getId(), data));
		}
	}
	
	@Override
	public void sendPairingData(ServerPlayer player, Consumer<CustomPacketPayload> packetConsumer) {
		super.sendPairingData(player, packetConsumer);
		ProfileData data = getData(ModAttachments.PROFILE_DATA.get());
		packetConsumer.accept(new SyncAppearanceToClientPacket(getId(), data));
	}
	
	@Override
	public void onAddedToLevel() {
		super.onAddedToLevel();
		if (!level().isClientSide()) {
			if (getBirthTime().isEmpty()) {
				Holder<Timeline> timelineHolder = level().registryAccess().getOrThrow(Timelines.EARLY_GAME);
				long timeDay = timelineHolder.value().getCurrentTicks(level().getServer().clockManager());
				long timeInDay = timeDay % 24000;
				
				int day = (int) (timeDay / 24000);
				int h = (int) ((timeInDay / 1000 + 6) % 24);
				int m = (int) ((timeInDay % 1000 * 3) / 50);
				
				setBirthTime(String.format("%d%02d%02d", day, h, m));
			}
			isReadyToInitializeProfile = true;
			getOrCreateProfile();
			refreshBrain((ServerLevel) level());
			syncAppearanceToTracking();
		}
	}
	
	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		input.read("birth_time", Codec.STRING).ifPresent(this::setBirthTime);
		input.read("profile_data", ProfileData.CODEC.codec()).ifPresent(newData -> {
			setData(ModAttachments.PROFILE_DATA.get(), newData.withEntity(this));
			setCustomName(newData.displayName());
		});
		input.read("relationships", RelationshipData.CODEC.listOf()).ifPresent(list -> {
			NvProfile prof = getOrCreateProfile();
			prof.clearRelationships();
			for (RelationshipData rel : list) {
				RelationshipData resolvedRel = new RelationshipData(
						rel.getTargetUuid(),
						prof.getPersonality(),
						rel.getAllValues(),
						rel.getRelationshipTier(),
						rel.getLastInteractionTime()
				);
				prof.setRelationship(resolvedRel);
			}
		});
	}
	
	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		String birth = getBirthTime();
		if (!birth.isEmpty())
			output.store("birth_time", Codec.STRING, birth);
		ProfileData data = getData(ModAttachments.PROFILE_DATA.get());
		output.store("profile_data", ProfileData.CODEC.codec(), data);
		output.store("relationships", RelationshipData.CODEC.listOf(), List.copyOf(getOrCreateProfile().getRelationships().values()));
	}
	
	public String getCuid() {
		ProfileData data = getData(ModAttachments.PROFILE_DATA.get());
		if (data.entity() != this) {
			data = data.withEntity(this);
			setData(ModAttachments.PROFILE_DATA.get(), data);
		}
		return data.getCuid();
	}
}
