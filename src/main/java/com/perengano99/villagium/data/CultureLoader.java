package com.perengano99.villagium.data;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.Culture;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CultureLoader extends SimpleJsonResourceReloadListener<Culture> {
	
	private static final Logger LOGGER = Logger.getLogger();
	
	public CultureLoader() {
		super(Culture.CODEC, FileToIdConverter.json("nv_cultures"));
	}
	
	@Override
	protected void apply(@NotNull Map<Identifier, Culture> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.CULTURES.clear();
		LOGGER.info("Loading NPC cultures from datapacks...");
		
		resources.forEach((location, culture) -> {
			Identifier fileId = Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath());
			// Inject location ID to ensure matching key mapping
			Culture resolved = new Culture(
					fileId,
					culture.displayKey(),
					culture.cuidKey(),
					culture.civilizationParams(),
					culture.rules()
			);
			if (VillagiumData.CULTURES.containsKey(fileId))
				LOGGER.warn("Duplicate culture ID '{}' in {}. Overwriting.", fileId, location);
			else
				VillagiumData.CULTURES.put(fileId, resolved);
		});
		
		LOGGER.info("Loaded {} cultures in total!", VillagiumData.CULTURES.size());
	}
}
