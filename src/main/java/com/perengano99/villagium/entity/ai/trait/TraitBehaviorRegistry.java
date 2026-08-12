package com.perengano99.villagium.entity.ai.trait;

import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TraitBehaviorRegistry {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Map<Identifier, TraitBehaviorFactory<?>> REGISTRY = new HashMap<>();

	public static void register(Identifier behaviorId, TraitBehaviorFactory<?> behaviorFactory) {
		if (REGISTRY.containsKey(behaviorId)) {
			LOGGER.warn("Se está intentando registrar un tipo de TraitBehaviorFactory duplicado: {}", behaviorId);
			return;
		}
		REGISTRY.put(behaviorId, behaviorFactory);
		LOGGER.debug("Registrado un TraitBehaviorFactory: {}", behaviorId);
	}

	public static Optional<TraitBehaviorFactory<?>> get(Identifier id) {
		return Optional.ofNullable(REGISTRY.get(id));
	}
}
