package com.perengano99.villagium.social.trait;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public interface TraitModifier {
	Codec<TraitModifier> CODEC = Identifier.CODEC.dispatch(
			TraitModifier::type,
			TraitModifierRegistry::getCodec
	);

	Identifier type();
}
