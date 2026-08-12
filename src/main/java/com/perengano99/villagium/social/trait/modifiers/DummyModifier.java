package com.perengano99.villagium.social.trait.modifiers;

import com.mojang.serialization.MapCodec;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.resources.Identifier;

public record DummyModifier() implements TraitModifier {
	public static final MapCodec<DummyModifier> CODEC = MapCodec.unit(new DummyModifier());

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "dummy");
	}
}
