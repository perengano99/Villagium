package com.perengano99.villagium.social.trait.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;
import java.util.Map;

public record ReceptionModifier(Map<String, Integer> tagScores) implements TraitModifier {
	public static final MapCodec<ReceptionModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("category_scores").forGetter(ReceptionModifier::tagScores)
	).apply(instance, ReceptionModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "reception_score");
	}
}
