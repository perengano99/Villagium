package com.perengano99.villagium.social.relationship;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.profile.NvProfile;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record RelationTag(
		Identifier id,
		int priority,
		int baseWeight,
		int receptionModifier,
		Optional<Map<RelationshipAxis, AxisCondition>> conditions,
		Optional<List<Map<RelationshipAxis, AxisCondition>>> conditionsOr,
		Optional<List<Identifier>> requiredTraits,
		Optional<List<Identifier>> requiredTargetTraits,
		Optional<Map<Identifier, Integer>> traitModifiers,
		Optional<Boolean> requiredKnown,
		String displayKey,
		int displayColor
) {

	public static final Codec<Integer> COLOR_CODEC = Codec.STRING.comapFlatMap(
			str -> {
				try {
					String hex = str.startsWith("#") ? str.substring(1) : str;
					if (hex.length() <= 6)
						return DataResult.success(0xFF000000 | Integer.parseInt(hex, 16));
					return DataResult.success((int) Long.parseLong(hex, 16));
				} catch (NumberFormatException e) {
					return DataResult.error(() -> "Invalid color format: " + str);
				}
			},
			color -> {
				if ((color & 0xFF000000) == 0xFF000000)
					return String.format("#%06X", color & 0xFFFFFF);
				return String.format("#%08X", color);
			}
	);
	
	public static final Codec<RelationTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(RelationTag::id),
			Codec.INT.fieldOf("priority").forGetter(RelationTag::priority),
			Codec.INT.fieldOf("base_weight").forGetter(RelationTag::baseWeight),
			Codec.INT.optionalFieldOf("reception_modifier", 0).forGetter(RelationTag::receptionModifier),
			Codec.unboundedMap(RelationshipAxis.CODEC, AxisCondition.CODEC).optionalFieldOf("conditions").forGetter(RelationTag::conditions),
			Codec.unboundedMap(RelationshipAxis.CODEC, AxisCondition.CODEC).listOf().optionalFieldOf("conditions_or").forGetter(RelationTag::conditionsOr),
			Identifier.CODEC.listOf().optionalFieldOf("required_traits").forGetter(RelationTag::requiredTraits),
			Identifier.CODEC.listOf().optionalFieldOf("required_target_traits").forGetter(RelationTag::requiredTargetTraits),
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("trait_modifiers").forGetter(RelationTag::traitModifiers),
			Codec.BOOL.optionalFieldOf("required_known").forGetter(RelationTag::requiredKnown),
			Codec.STRING.fieldOf("display_key").forGetter(RelationTag::displayKey),
			COLOR_CODEC.fieldOf("display_color").forGetter(RelationTag::displayColor)
	                                                                                                   ).apply(instance, RelationTag::new));
	
	public int calculateWeight(RelationshipData data, NvProfile ownerProfile, @Nullable NvProfile targetProfile, boolean isKnown) {
		if (requiredKnown.isPresent() && requiredKnown.get() != isKnown)
			return 0;
		
		if (conditions.isPresent() && !checkConditions(conditions.get(), data))
			return 0;
		
		if (conditionsOr.isPresent()) {
			boolean matchedAny = false;
			for (Map<RelationshipAxis, AxisCondition> conds : conditionsOr.get()) {
				if (checkConditions(conds, data)) {
					matchedAny = true;
					break;
				}
			}
			if (!matchedAny)
				return 0;
		}
		
		if (requiredTraits.isPresent()) {
			for (Identifier traitId : requiredTraits.get()) {
				if (!ownerProfile.hasTrait(traitId))
					return 0;
			}
		}
		
		if (requiredTargetTraits.isPresent() && targetProfile != null) {
			for (Identifier traitId : requiredTargetTraits.get()) {
				if (!targetProfile.hasTrait(traitId))
					return 0;
			}
		}
		
		int weight = baseWeight;
		if (traitModifiers.isPresent()) {
			for (Map.Entry<Identifier, Integer> entry : traitModifiers.get().entrySet()) {
				if (ownerProfile.hasTrait(entry.getKey()))
					weight += entry.getValue();
			}
		}
		return weight;
	}
	
	private boolean checkConditions(Map<RelationshipAxis, AxisCondition> conds, RelationshipData data) {
		for (Map.Entry<RelationshipAxis, AxisCondition> entry : conds.entrySet()) {
			if (!entry.getValue().matches(data.getValue(entry.getKey())))
				return false;
		}
		return true;
	}
	
	public record AxisCondition(Optional<Float> min, Optional<Float> max) {
		
		public static final AxisCondition ANY = new AxisCondition(Optional.empty(), Optional.empty());
		
		public boolean matches(float value) {
			if (min.isPresent() && value < min.get())
				return false;
			return max.isEmpty() || !(value > max.get());
		}
		
		public static final Codec<AxisCondition> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("min").forGetter(AxisCondition::min),
				Codec.FLOAT.optionalFieldOf("max").forGetter(AxisCondition::max)).apply(instance, AxisCondition::new));
		
		public static final Codec<AxisCondition> CODEC = Codec.either(Codec.FLOAT, RECORD_CODEC).xmap(
				either -> either.map(
						exact -> new AxisCondition(Optional.of(exact), Optional.of(exact)),
						cond -> cond),
				cond -> {
					if (cond.min().isPresent() && cond.max().isPresent() && cond.min().get().equals(cond.max().get()))
						return Either.left(cond.min().get());
					return Either.right(cond);
				});
	}
}
