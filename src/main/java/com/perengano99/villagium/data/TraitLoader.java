package com.perengano99.villagium.data;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.trait.Trait;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TraitLoader extends SimpleJsonResourceReloadListener<Trait> {
	private static final Logger LOGGER = Logger.getLogger();

	public TraitLoader() {
		super(Trait.CODEC, FileToIdConverter.json("nv_traits"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, Trait> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.TRAITS.clear();
		LOGGER.info("Loading NPC traits from datapacks...");

		resources.forEach((location, trait) -> {
			Identifier fileId = Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath());
			// Inject location ID to ensure matching key mapping
			Trait resolved = new Trait(
					fileId,
					trait.displayKey(),
					trait.descriptionKey(),
					trait.modifiers()
			);
			if (VillagiumData.TRAITS.containsKey(fileId))
				LOGGER.warn("Duplicate trait ID '{}' in {}. Overwriting.", fileId, location);
			else
				VillagiumData.TRAITS.put(fileId, resolved);
		});

		LOGGER.info("Loaded {} traits in total!", VillagiumData.TRAITS.size());
	}
}
