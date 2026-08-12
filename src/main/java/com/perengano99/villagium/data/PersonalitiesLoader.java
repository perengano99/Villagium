package com.perengano99.villagium.data;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.NvPersonality;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PersonalitiesLoader extends SimpleJsonResourceReloadListener<NvPersonality> {
	private static final Logger LOGGER = Logger.getLogger();

	public PersonalitiesLoader() {
		super(NvPersonality.CODEC, FileToIdConverter.json("nv_personalities"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, NvPersonality> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.PERSONALITIES.clear();
		LOGGER.info("Loading NPC personalities from datapacks...");

		resources.forEach((location, personality) -> {
			Identifier fileId = Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath());
			// Inject location ID to ensure matching key mapping
			NvPersonality resolved = new NvPersonality(
					fileId,
					personality.getDisplayNameKey(),
					personality.getPreferences(),
					personality.getSensitivities()
			);
			if (VillagiumData.PERSONALITIES.containsKey(fileId))
				LOGGER.warn("Duplicate personality ID '{}' in {}. Overwriting.", fileId, location);
			else
				VillagiumData.PERSONALITIES.put(fileId, resolved);
		});

		LOGGER.info("Loaded {} personalities in total!", VillagiumData.PERSONALITIES.size());
	}
}
