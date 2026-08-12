package com.perengano99.villagium.social.trait;

import com.mojang.serialization.MapCodec;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.trait.modifiers.*;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TraitModifierRegistry {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Map<Identifier, MapCodec<? extends TraitModifier>> REGISTRY = new ConcurrentHashMap<>();

	private TraitModifierRegistry() {}

	public static void register(Identifier id, MapCodec<? extends TraitModifier> codec) {
		if (REGISTRY.containsKey(id)) {
			LOGGER.warn("Se está intentando registrar un tipo de TraitModifier duplicado: {}", id);
			return;
		}
		REGISTRY.put(id, codec);
	}

	public static MapCodec<? extends TraitModifier> getCodec(Identifier id) {
		MapCodec<? extends TraitModifier> codec = REGISTRY.get(id);
		if (codec == null) {
			LOGGER.warn("Unknown trait/mood modifier type '{}', using dummy fallback.", id);
			return DummyModifier.CODEC;
		}
		return codec;
	}

	static {
		register(Identifier.fromNamespaceAndPath("villagium", "reception_score"), ReceptionModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "hunger_modifier"), HungerModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "preferred_food"), PreferredFoodModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "add_behavior"), AddBehaviorModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "damage_modifier"), DamageModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "conditional_effect"), ConditionalEffectModifier.CODEC);
		register(Identifier.fromNamespaceAndPath("villagium", "dummy"), DummyModifier.CODEC);
	}
}
