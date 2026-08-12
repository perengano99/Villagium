package com.perengano99.villagium.social.event;

import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SocialEventRegistry {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Map<Identifier, SocialEvent> REGISTRY = new ConcurrentHashMap<>();

	private SocialEventRegistry() {}

	public static void register(Identifier id, SocialEvent event) {
		if (REGISTRY.putIfAbsent(id, event) != null)
			LOGGER.warn("Duplicate social event registration for ID: {}", id);
	}

	@Nullable
	public static SocialEvent get(Identifier id) {
		return REGISTRY.get(id);
	}
}
