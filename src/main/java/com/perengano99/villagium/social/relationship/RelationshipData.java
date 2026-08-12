package com.perengano99.villagium.social.relationship;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.social.profile.NvProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class RelationshipData {
	
	private final UUID targetUuid;
	private final Map<RelationshipAxis, Float> values;
	private final List<TemporaryModifier> temporaryModifiers = new ArrayList<>();
	private final Map<Identifier, Integer> activeTags = new HashMap<>();
	private Identifier primaryTag = Identifier.fromNamespaceAndPath(Villagium.MODID, "stranger");
	@Nullable
	private Instant lastInteractionTime = null;
	
	public RelationshipData(UUID targetUuid) {
		this.targetUuid = targetUuid;
		this.values     = new EnumMap<>(RelationshipAxis.class);
		for (RelationshipAxis axis : RelationshipAxis.values())
			values.put(axis, 0f);
	}
	
	public RelationshipData(UUID targetUuid, Map<RelationshipAxis, Float> values, List<TemporaryModifier> temporaryModifiers,
	                        Map<Identifier, Integer> activeTags, Identifier primaryTag, @Nullable Instant lastInteractionTime) {
		this.targetUuid = targetUuid;
		this.values     = new EnumMap<>(values);
		this.temporaryModifiers.addAll(temporaryModifiers);
		this.activeTags.putAll(activeTags);
		this.primaryTag          = primaryTag;
		this.lastInteractionTime = lastInteractionTime;
	}
	
	public UUID getTargetUuid() {
		return targetUuid;
	}
	
	public void addValue(RelationshipAxis axis, float value) {
		float newValue = values.getOrDefault(axis, 0f) + value;
		setValue(axis, newValue);
	}
	
	public void setValue(RelationshipAxis axis, float value) {
		float clampedValue = clampAxisValue(axis, value);
		values.put(axis, clampedValue);
	}
	
	public float getValue(RelationshipAxis axis) {
		float base = values.getOrDefault(axis, 0f);
		float modSum = 0f;
		for (TemporaryModifier mod : temporaryModifiers) {
			if (mod.axis().isPresent() && mod.axis().get() == axis)
				modSum += mod.axisAmount();
		}
		return Math.clamp(axis.getMax(), axis.getMin(), base + modSum);
	}
	
	public Map<RelationshipAxis, Float> getAllValues() {
		return Collections.unmodifiableMap(values);
	}
	
	private float clampAxisValue(RelationshipAxis axis, float val) {
		val = Math.clamp(val, axis.getMin(), axis.getMax());
		if (axis == RelationshipAxis.LOVE) {
			float resentment = values.getOrDefault(RelationshipAxis.RESENTMENT, 0f);
			val = Math.min(val, 100f - resentment);
		} else if (axis == RelationshipAxis.RESENTMENT) {
			float love = values.getOrDefault(RelationshipAxis.LOVE, 0f);
			float valCap = 100f - love;
			float friendship = values.getOrDefault(RelationshipAxis.FRIENDSHIP, 0f);
			if (friendship > 0f)
				valCap = Math.min(valCap, 100f - friendship);
			val = Math.min(val, valCap);
		} else if (axis == RelationshipAxis.FRIENDSHIP && val > 0f) {
			float resentment = values.getOrDefault(RelationshipAxis.RESENTMENT, 0f);
			val = Math.min(val, 100f - resentment);
		}
		return val;
	}
	
	public void addTemporaryModifier(TemporaryModifier modifier) {
		temporaryModifiers.add(modifier);
	}
	
	public List<TemporaryModifier> getTemporaryModifiers() {
		return Collections.unmodifiableList(temporaryModifiers);
	}
	
	public boolean tickModifiers() {
		if (temporaryModifiers.isEmpty())
			return false;
		boolean expiredAny = false;
		for (int i = temporaryModifiers.size() - 1; i >= 0; i--) {
			TemporaryModifier mod = temporaryModifiers.get(i).tick();
			if (mod.isExpired()) {
				temporaryModifiers.remove(i);
				expiredAny = true;
			} else
				temporaryModifiers.set(i, mod);
		}
		return expiredAny;
	}
	
	@Nullable
	public Instant getLastInteractionTime() {
		return lastInteractionTime;
	}
	
	public void updateLastInteractionTime() {
		this.lastInteractionTime = Instant.now();
	}
	
	public Map<Identifier, Integer> getActiveTags() {
		return Collections.unmodifiableMap(activeTags);
	}
	
	public Identifier getPrimaryTag() {
		return primaryTag;
	}
	
	public void updateRelationshipTags(NvProfile ownerProfile, @Nullable NvProfile targetProfile, boolean isKnown) {
		activeTags.clear();
		int highestScore = -1;
		RelationTag primary = null;
		
		for (RelationTag tag : VillagiumData.RELATION_TAGS.values()) {
			int score = tag.calculateWeight(this, ownerProfile, targetProfile, isKnown);
			if (score > 0) {
				for (TemporaryModifier mod : temporaryModifiers) {
					if (mod.tagId().isPresent() && mod.tagId().get().equals(tag.id()))
						score += mod.tagWeight();
				}
				score = Math.max(1, score);
				activeTags.put(tag.id(), score);
				if (score > highestScore || score == highestScore && tag.priority() > primary.priority()) {
					highestScore = score;
					primary      = tag;
				}
			}
		}
		
		if (activeTags.isEmpty()) {
			Identifier defaultTag = isKnown ?
			                        Identifier.fromNamespaceAndPath("villagium", "neutral") :
			                        Identifier.fromNamespaceAndPath("villagium", "stranger");
			this.primaryTag = defaultTag;
			activeTags.put(defaultTag, 1);
		} else {
			this.primaryTag = primary != null ? primary.id() : Identifier.fromNamespaceAndPath("villagium", "stranger");
		}
	}
	
	public void applyDecay(RelationshipAxis axis, float targetValue, float decayRate) {
		float current = values.getOrDefault(axis, 0f);
		float diff = targetValue - current;
		float newValue = current + diff * decayRate;
		setValue(axis, newValue);
	}
	
	public static final Codec<RelationshipData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					UUIDUtil.CODEC.fieldOf("target").forGetter(RelationshipData::getTargetUuid),
					Codec.unboundedMap(RelationshipAxis.CODEC, Codec.FLOAT).fieldOf("values").forGetter(r -> r.values),
					TemporaryModifier.CODEC.listOf().optionalFieldOf("temporary_modifiers", List.of()).forGetter(r -> r.temporaryModifiers),
					Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("active_tags", Map.of()).forGetter(r -> r.activeTags),
					Identifier.CODEC.optionalFieldOf("primary_tag", Identifier.fromNamespaceAndPath(Villagium.MODID, "stranger")).forGetter(r -> r.primaryTag),
					Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli).optionalFieldOf("last_interaction").forGetter(r -> Optional.ofNullable(r.getLastInteractionTime()))
	                                                                                                        )
			.apply(instance, (target, valMap, modifiers, tags, primary, lastInteraction) -> new RelationshipData(
					target, new EnumMap<>(valMap), new ArrayList<>(modifiers), new HashMap<>(tags), primary, lastInteraction.orElse(null)
			)));
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RelationshipData that = (RelationshipData) o;
		return targetUuid.equals(that.targetUuid);
	}
	
	@Override
	public int hashCode() {
		return targetUuid.hashCode();
	}
	
	@Override
	public String toString() {
		return "RelationshipData{" + "targetUuid=" + targetUuid + ", values=" + values + ", primaryTag=" + primaryTag + ", lastInteractionTime=" + lastInteractionTime + '}';
	}
	
	public ClientData toClientData() {
		return ClientData.from(this);
	}
	
	public record ClientData(
			UUID targetUuid,
			Map<RelationshipAxis, Float> values,
			List<TemporaryModifier> temporaryModifiers,
			Map<Identifier, Integer> activeTags,
			Identifier primaryTag,
			@Nullable Instant lastInteractionTime
	) {
		
		public static final StreamCodec<FriendlyByteBuf, ClientData> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public @NotNull ClientData decode(FriendlyByteBuf buf) {
				return new ClientData(
						buf.readUUID(),
						buf.readMap(m -> new EnumMap<>(RelationshipAxis.class), b -> b.readEnum(RelationshipAxis.class), FriendlyByteBuf::readFloat),
						buf.readCollection(ArrayList::new, TemporaryModifier.STREAM_CODEC),
						buf.readMap(HashMap::new, Identifier.STREAM_CODEC, FriendlyByteBuf::readVarInt),
						Identifier.STREAM_CODEC.decode(buf),
						buf.readOptional(b -> Instant.ofEpochMilli(b.readLong())).orElse(null)
				);
			}
			
			@Override
			public void encode(FriendlyByteBuf buf, ClientData data) {
				buf.writeUUID(data.targetUuid());
				buf.writeMap(data.values(), FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeFloat);
				buf.writeCollection(data.temporaryModifiers(), TemporaryModifier.STREAM_CODEC);
				buf.writeMap(data.activeTags(), Identifier.STREAM_CODEC, FriendlyByteBuf::writeVarInt);
				Identifier.STREAM_CODEC.encode(buf, data.primaryTag());
				buf.writeOptional(Optional.ofNullable(data.lastInteractionTime()), (b, val) -> b.writeLong(val.toEpochMilli()));
			}
		};
		
		public static ClientData from(RelationshipData data) {
			return new ClientData(
					data.getTargetUuid(),
					data.getAllValues(),
					data.getTemporaryModifiers(),
					data.getActiveTags(),
					data.getPrimaryTag(),
					data.getLastInteractionTime()
			);
		}
		
		public float getValue(RelationshipAxis axis) {
			float base = values.getOrDefault(axis, 0f);
			float modSum = 0f;
			for (TemporaryModifier mod : temporaryModifiers)
				if (mod.axis().isPresent() && mod.axis().get() == axis)
					modSum += mod.axisAmount();
			return Math.clamp(axis.getMax(), axis.getMin(), base + modSum);
		}
	}
}
