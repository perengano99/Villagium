package com.perengano99.villagium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class SocialEventLoader extends SimpleJsonResourceReloadListener<SocialEventLoader.SocialEventsJson> {
	private static final Logger LOGGER = Logger.getLogger();

	public record SocialEventsJson(List<SocialEventDefinition> events) {
		public static final Codec<SocialEventsJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SocialEventDefinition.CODEC.listOf().fieldOf("events").forGetter(SocialEventsJson::events)
		).apply(instance, SocialEventsJson::new));
	}

	public SocialEventLoader() {
		super(SocialEventsJson.CODEC, FileToIdConverter.json("nv_social_events"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, SocialEventsJson> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.SOCIAL_EVENTS.clear();
		LOGGER.info("Loading dynamic NPC social events from datapacks...");

		resources.forEach((location, json) -> {
			for (SocialEventDefinition eventDef : json.events())
				if (VillagiumData.SOCIAL_EVENTS.containsKey(eventDef.id()))
					LOGGER.warn("Duplicate social event ID '{}' in {}. Overwriting.", eventDef.id(), location);
				else
					VillagiumData.SOCIAL_EVENTS.put(eventDef.id(), eventDef);
		});

		LOGGER.info("Loaded {} social events in total!", VillagiumData.SOCIAL_EVENTS.size());
	}
}
