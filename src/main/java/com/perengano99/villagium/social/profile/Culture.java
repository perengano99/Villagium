package com.perengano99.villagium.social.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Culture(
		Identifier id,
		String displayKey,
		String cuidKey,
		CivilizationParams civilizationParams,
		Rules rules
) {
	public static final Codec<Culture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("id", Identifier.fromNamespaceAndPath("villagium", "generic")).forGetter(Culture::id),
			Codec.STRING.fieldOf("display_key").forGetter(Culture::displayKey),
			Codec.STRING.fieldOf("cuid_key").orElse("GEN").forGetter(Culture::cuidKey),
			CivilizationParams.CODEC.fieldOf("civilization_params").orElse(CivilizationParams.defaults()).forGetter(Culture::civilizationParams),
			Rules.CODEC.fieldOf("rules").orElse(Rules.defaults()).forGetter(Culture::rules)
	).apply(instance, Culture::new));

	public record CivilizationParams(
			List<String> climates,
			HeightRange heightRange,
			List<Identifier> biomes,
			Map<String, String> otherFactors
	) {
		public static final Codec<CivilizationParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().fieldOf("climates").orElse(List.of()).forGetter(CivilizationParams::climates),
				HeightRange.CODEC.fieldOf("height_range").orElse(HeightRange.ANY).forGetter(CivilizationParams::heightRange),
				Identifier.CODEC.listOf().fieldOf("biomes").orElse(List.of()).forGetter(CivilizationParams::biomes),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("other_factors").orElse(Map.of()).forGetter(CivilizationParams::otherFactors)
		).apply(instance, CivilizationParams::new));

		public static CivilizationParams defaults() {
			return new CivilizationParams(List.of(), HeightRange.ANY, List.of(), Map.of());
		}
	}

	public record HeightRange(Optional<Integer> min, Optional<Integer> max) {
		public static final HeightRange ANY = new HeightRange(Optional.empty(), Optional.empty());
		public static final Codec<HeightRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf("min").forGetter(HeightRange::min),
				Codec.INT.optionalFieldOf("max").forGetter(HeightRange::max)
		).apply(instance, HeightRange::new));
	}

	public record Rules(
			Optional<String> race,
			boolean isNomadic
	) {
		public static final Codec<Rules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("race").forGetter(Rules::race),
				Codec.BOOL.fieldOf("is_nomadic").orElse(false).forGetter(Rules::isNomadic)
		).apply(instance, Rules::new));

		public static Rules defaults() {
			return new Rules(Optional.empty(), false);
		}
	}
}
