package com.perengano99.villagium.social.trait.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import java.util.Map;

public record ConditionalEffectModifier(
		Map<String, Float> tagMultipliers,
		JsonObject conditions
) implements TraitModifier {
	public static final MapCodec<ConditionalEffectModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("tag_multipliers").forGetter(ConditionalEffectModifier::tagMultipliers),
			ExtraCodecs.JSON.xmap(
					JsonElement::getAsJsonObject,
					el -> el
			).fieldOf("conditions").orElse(new JsonObject()).forGetter(ConditionalEffectModifier::conditions)
	).apply(instance, ConditionalEffectModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "conditional_effect");
	}
}
