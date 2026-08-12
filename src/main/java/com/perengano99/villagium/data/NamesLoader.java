package com.perengano99.villagium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.NvGender;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NamesLoader extends SimpleJsonResourceReloadListener<NamesLoader.NamesJson> {
	
	private static final Logger LOGGER = Logger.getLogger();
	private static final Random RND = new Random();
	
	public record NameGroup(List<String> male, List<String> female, List<String> bisexual) {}
	
	public record NamesJson(String method, List<String> male, List<String> female, List<String> bisexual) {
		public static final Codec<NamesJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("method").orElse("REPLACE").forGetter(NamesJson::method),
				Codec.STRING.listOf().fieldOf("male").orElse(List.of()).forGetter(NamesJson::male),
				Codec.STRING.listOf().fieldOf("female").orElse(List.of()).forGetter(NamesJson::female),
				Codec.STRING.listOf().fieldOf("bisexual").orElse(List.of()).forGetter(NamesJson::bisexual)
		).apply(instance, NamesJson::new));
	}
	
	private static final Map<String, NameGroup> GROUPS = new ConcurrentHashMap<>();
	
	public NamesLoader() {
		super(NamesJson.CODEC, FileToIdConverter.json("nv_names"));
	}
	
	@Override
	protected void apply(@NotNull Map<Identifier, NamesJson> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		GROUPS.clear();
		LOGGER.info("Loading dynamic NPC names from datapacks...");
		
		resources.forEach((location, json) -> {
			boolean hasMale = json.male != null && !json.male.isEmpty();
			boolean hasFemale = json.female != null && !json.female.isEmpty();
			boolean hasBisexual = json.bisexual != null && !json.bisexual.isEmpty();
			
			if (!hasMale && !hasFemale && !hasBisexual) {
				LOGGER.error("Invalid names datapack {}: must contain at least one non-empty gender list.", location);
				return;
			}
			
			String group = location.getPath().toLowerCase(Locale.ROOT);
			String method = json.method != null ? json.method.toUpperCase(Locale.ROOT) : "REPLACE";
			
			if ("MERGE".equals(method) && GROUPS.containsKey(group)) {
				NameGroup existing = GROUPS.get(group);
				List<String> mergedMale = mergeLists(existing.male(), json.male);
				List<String> mergedFemale = mergeLists(existing.female(), json.female);
				List<String> mergedBisexual = mergeLists(existing.bisexual(), json.bisexual);
				GROUPS.put(group, new NameGroup(mergedMale, mergedFemale, mergedBisexual));
			} else {
				List<String> maleList = json.male != null ? List.copyOf(json.male) : List.of();
				List<String> femaleList = json.female != null ? List.copyOf(json.female) : List.of();
				List<String> bisexualList = json.bisexual != null ? List.copyOf(json.bisexual) : List.of();
				GROUPS.put(group, new NameGroup(maleList, femaleList, bisexualList));
			}
		});
		
		LOGGER.info("Loaded {} name group(s) successfully!", GROUPS.size());
	}
	
	private static List<String> mergeLists(List<String> existing, List<String> incoming) {
		if (incoming == null || incoming.isEmpty())
			return existing;
		Set<String> merged = new LinkedHashSet<>(existing);
		merged.addAll(incoming);
		return List.copyOf(merged);
	}
	
	public static String getRandomName(String group, NvGender gender) {
		NameGroup nameGroup = GROUPS.get(group.toLowerCase(Locale.ROOT));
		if (nameGroup == null)
			return getDefaultFallbackName(gender);
		
		List<String> pool = switch (gender) {
			case FEMALE -> nameGroup.female();
			case MALE -> nameGroup.male();
			default -> nameGroup.bisexual();
		};
		
		if (pool.isEmpty()) {
			if (!nameGroup.bisexual().isEmpty())
				pool = nameGroup.bisexual();
			else if (gender == NvGender.FEMALE && !nameGroup.male().isEmpty())
				pool = nameGroup.male();
			else if (gender == NvGender.MALE && !nameGroup.female().isEmpty())
				pool = nameGroup.female();
			else
				return getDefaultFallbackName(gender);
		}
		
		return pool.get(RND.nextInt(pool.size()));
	}
	
	private static String getDefaultFallbackName(NvGender gender) {
		return switch (gender) {
			case FEMALE -> "Villagera";
			case MALE -> "Villager";
			default -> "NPC";
		};
	}
}
