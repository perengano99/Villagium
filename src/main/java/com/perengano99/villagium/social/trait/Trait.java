package com.perengano99.villagium.social.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.List;

public record Trait(
		Identifier id,
		String displayKey,
		String descriptionKey,
		List<TraitModifier> modifiers
) {
	public static final Codec<Trait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(Trait::id),
			Codec.STRING.fieldOf("display_key").forGetter(Trait::displayKey),
			Codec.STRING.fieldOf("description_key").forGetter(Trait::descriptionKey),
			TraitModifier.CODEC.listOf().fieldOf("modifiers").orElse(List.of()).forGetter(Trait::modifiers)
	).apply(instance, Trait::new));
}
