package com.perengano99.villagium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.Mood;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MoodLoader extends SimpleJsonResourceReloadListener<MoodLoader.MoodsJson> {
	private static final Logger LOGGER = Logger.getLogger();

	public record MoodsJson(List<Mood> moods) {
		public static final Codec<MoodsJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Mood.CODEC.listOf().fieldOf("moods").forGetter(MoodsJson::moods)
		).apply(instance, MoodsJson::new));
	}

	public MoodLoader() {
		super(MoodsJson.CODEC, FileToIdConverter.json("nv_moods"));
	}

	@Override
	protected void apply(@NotNull Map<Identifier, MoodsJson> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		VillagiumData.MOODS.clear();
		LOGGER.info("Loading NPC moods from datapacks...");

		resources.forEach((location, json) -> {
			if (json.moods() == null)
				return;

			for (Mood mood : json.moods()) {
				String moodId = mood.id();
				Mood existing = VillagiumData.MOODS.get(moodId);

				if (existing != null) {
					if (mood.moodModifiers().isEmpty())
						LOGGER.warn("Omitted registering duplicate mood '{}' as it has no modifiers.", moodId);
					else {
						existing.addModifiers(mood.moodModifiers());
						LOGGER.info("Added modifiers to existing mood '{}' from duplicate registration.", moodId);
					}
				} else
					VillagiumData.MOODS.put(moodId, mood);
			}
		});

		LOGGER.info("Loaded {} moods in total!", VillagiumData.MOODS.size());
	}
}
