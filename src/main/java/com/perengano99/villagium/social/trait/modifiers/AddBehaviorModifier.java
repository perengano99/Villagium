package com.perengano99.villagium.social.trait.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record AddBehaviorModifier(
		Identifier behavior,
		Identifier activity,
		int priority,
		JsonObject data
) implements TraitModifier {

	public static final MapCodec<AddBehaviorModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("behavior").forGetter(AddBehaviorModifier::behavior),
			Codec.STRING.xmap(
					s -> s.contains(":") ? Identifier.parse(s) : Identifier.fromNamespaceAndPath("minecraft", s),
					Identifier::toString
			).fieldOf("activity").forGetter(AddBehaviorModifier::activity),
			Codec.INT.fieldOf("priority").orElse(1).forGetter(AddBehaviorModifier::priority),
			ExtraCodecs.JSON.xmap(
					JsonElement::getAsJsonObject,
					el -> el
			).fieldOf("data").orElse(new JsonObject()).forGetter(AddBehaviorModifier::data)
	).apply(instance, AddBehaviorModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "add_behavior");
	}
}
