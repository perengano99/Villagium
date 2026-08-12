package com.perengano99.villagium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.dialogue.DialogueEntry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialoguesLoader extends SimpleJsonResourceReloadListener<DialoguesLoader.DialoguesJson> {
	private static final Logger LOGGER = Logger.getLogger();

	public record DialoguesJson(Identifier personality, List<DialogueEntry> entries) {
		public static final Codec<DialoguesJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("personality").forGetter(DialoguesJson::personality),
				DialogueEntry.CODEC.listOf().fieldOf("entries").forGetter(DialoguesJson::entries)
		).apply(instance, DialoguesJson::new));
	}

	public DialoguesLoader() {
		super(DialoguesJson.CODEC, FileToIdConverter.json("nv_dialogues"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, DialoguesJson> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.DIALOGUES.clear();
		LOGGER.info("Loading dynamic NPC dialogues from datapacks...");

		resources.forEach((location, json) -> {
			Identifier personalityId = json.personality();
			Map<String, DialogueEntry> dialogueMap = VillagiumData.DIALOGUES.computeIfAbsent(personalityId, k -> new HashMap<>());

			for (DialogueEntry entry : json.entries()) {
				// Inject personalityId from parent JSON context
				DialogueEntry entryWithPersonality = new DialogueEntry(
						entry.id(),
						personalityId,
						entry.type(),
						entry.event(),
						entry.conditions(),
						entry.translationKeys(),
						entry.weight(),
						entry.specificity()
				);
				if (dialogueMap.containsKey(entry.id()))
					LOGGER.warn("Duplicate dialogue entry ID '{}' in {}. Overwriting.", entry.id(), location);
				else
					dialogueMap.put(entry.id(), entryWithPersonality);
			}
		});

		long totalDialogues = VillagiumData.DIALOGUES.values().stream().mapToLong(Map::size).sum();
		LOGGER.info("Loaded {} dialogue entries in total!", totalDialogues);
	}
}
