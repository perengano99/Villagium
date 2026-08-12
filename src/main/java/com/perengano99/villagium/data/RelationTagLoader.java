package com.perengano99.villagium.data;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.relationship.RelationTag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class RelationTagLoader extends SimpleJsonResourceReloadListener<RelationTag> {
	private static final Logger LOGGER = Logger.getLogger();

	public RelationTagLoader() {
		super(RelationTag.CODEC, FileToIdConverter.json("nv_relation_tags"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, RelationTag> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.RELATION_TAGS.clear();
		LOGGER.info("Loading NPC relation tags from datapacks...");

		resources.forEach((location, tag) -> {
			Identifier fileId = Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath());
			RelationTag resolved = new RelationTag(
					fileId,
					tag.priority(),
					tag.baseWeight(),
					tag.receptionModifier(),
					tag.conditions(),
					tag.conditionsOr(),
					tag.requiredTraits(),
					tag.requiredTargetTraits(),
					tag.traitModifiers(),
					tag.requiredKnown(),
					tag.displayKey(),
					tag.displayColor()
			);
			if (VillagiumData.RELATION_TAGS.containsKey(fileId))
				LOGGER.warn("Duplicate relation tag ID '{}' in {}. Overwriting.", fileId, location);
			else
				VillagiumData.RELATION_TAGS.put(fileId, resolved);
		});

		LOGGER.info("Loaded {} relation tags in total!", VillagiumData.RELATION_TAGS.size());
	}
}
