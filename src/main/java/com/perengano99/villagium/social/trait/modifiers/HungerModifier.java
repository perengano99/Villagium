package com.perengano99.villagium.social.trait.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;

public record HungerModifier(
		float hungerThresholdMultiplier,
		float saturationBonusMultiplier
) implements TraitModifier {
	public static final HungerModifier DEFAULT = new HungerModifier(1.0f, 1.0f);

	public static final MapCodec<HungerModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.FLOAT.fieldOf("hungerThresholdMultiplier").orElse(1.0f).forGetter(HungerModifier::hungerThresholdMultiplier),
			Codec.FLOAT.fieldOf("saturationBonusMultiplier").orElse(1.0f).forGetter(HungerModifier::saturationBonusMultiplier)
	).apply(instance, HungerModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "hunger_modifier");
	}
}
