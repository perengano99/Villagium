package com.perengano99.villagium.social.trait.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;

public record DamageModifier(
		float multiplier,
		String damageType
) implements TraitModifier {
	public static final MapCodec<DamageModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.FLOAT.fieldOf("multiplier").orElse(1.0f).forGetter(DamageModifier::multiplier),
			Codec.STRING.fieldOf("damage_type").forGetter(DamageModifier::damageType)
	).apply(instance, DamageModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "damage_modifier");
	}
}
