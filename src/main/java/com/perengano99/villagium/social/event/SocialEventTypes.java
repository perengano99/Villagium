package com.perengano99.villagium.social.event;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.social.event.events.ConversationEvent;
import net.minecraft.resources.Identifier;

public final class SocialEventTypes {
	private SocialEventTypes() {}

	public static void initialize() {
		register("conversation", new ConversationEvent(path("conversation")));
	}

	private static void register(String path, SocialEvent event) {
		SocialEventRegistry.register(path(path), event);
	}

	private static Identifier path(String path) {
		return Identifier.fromNamespaceAndPath(Villagium.MODID, path);
	}
}
