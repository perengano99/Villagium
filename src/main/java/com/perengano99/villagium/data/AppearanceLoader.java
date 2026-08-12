package com.perengano99.villagium.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.NvGender;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppearanceLoader extends SimpleJsonResourceReloadListener<JsonElement> {
	
	private static final Logger LOGGER = Logger.getLogger();
	private static final Random RND = new Random();
	
	public static final Identifier MISSING_TEXTURE = Identifier.fromNamespaceAndPath(Villagium.MODID, "textures/nv_appearances/missing.png");

	public record TextureMetadata(
			Identifier id,
			String gender,
			List<String> cultures,
			List<String> professions
	) {}
	
	private static final Map<String, List<TextureMetadata>> SERVER_CATEGORIES = new ConcurrentHashMap<>();
	private static final Map<Identifier, Identifier> CLIENT_TEXTURES = new ConcurrentHashMap<>();
	
	public AppearanceLoader() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("nv_appearances"));
	}
	
	@Override
	protected void apply(@NotNull Map<Identifier, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		boolean hasMetadata = resources.values().stream().anyMatch(el -> el.isJsonObject() && el.getAsJsonObject().has("metadata"));
		boolean hasPath = resources.values().stream().anyMatch(el -> el.isJsonObject() && el.getAsJsonObject().has("path"));
		
		if (hasMetadata)
			SERVER_CATEGORIES.clear();
		if (hasPath)
			CLIENT_TEXTURES.clear();
		
		LOGGER.info("Loading appearances (unified)...");
		
		resources.forEach((location, element) -> {
			if (!element.isJsonObject())
				return;
			
			JsonObject obj = element.getAsJsonObject();
			String pathStr = location.getPath();
			int firstSlash = pathStr.indexOf('/');
			if (firstSlash == -1) {
				LOGGER.warn("Appearance definition at {} has invalid path format.", location);
				return;
			}
			
			String type = pathStr.substring(0, firstSlash);
			String filename = pathStr.substring(pathStr.lastIndexOf('/') + 1);
			Identifier fileId = Identifier.fromNamespaceAndPath(location.getNamespace(), filename);
			
			if (obj.has("metadata") && obj.get("metadata").isJsonObject()) {
				JsonObject meta = obj.getAsJsonObject("metadata");
				String gender = meta.has("gender") ? meta.get("gender").getAsString().toUpperCase(Locale.ROOT) : "ANY";
				List<String> cultures = new ArrayList<>();
				List<String> professions = new ArrayList<>();
				
				if (meta.has("cultures") && meta.get("cultures").isJsonArray()) {
					for (JsonElement c : meta.getAsJsonArray("cultures"))
						cultures.add(c.getAsString());
				}
				if (meta.has("professions") && meta.get("professions").isJsonArray()) {
					for (JsonElement p : meta.getAsJsonArray("professions"))
						professions.add(p.getAsString());
				}
				
				TextureMetadata metaDef = new TextureMetadata(fileId, gender, cultures, professions);
				SERVER_CATEGORIES.computeIfAbsent(type, k -> new ArrayList<>()).add(metaDef);
			}
			
			if (obj.has("path")) {
				String rawPath = obj.get("path").getAsString();
				Identifier pathId;
				if (rawPath.contains(":")) {
					Identifier parsed = Identifier.tryParse(rawPath);
					if (parsed == null)
						parsed = Identifier.fromNamespaceAndPath(location.getNamespace(), rawPath);
					pathId = Identifier.fromNamespaceAndPath(parsed.getNamespace(), "textures/" + parsed.getPath());
				} else
					pathId = Identifier.fromNamespaceAndPath(location.getNamespace(), "textures/" + rawPath);
				
				CLIENT_TEXTURES.put(fileId, pathId);
			}
		});
		
		LOGGER.info("Loaded {} server categories, {} client textures.", SERVER_CATEGORIES.size(), CLIENT_TEXTURES.size());
	}
	
	public static Identifier getRandomTextureId(String type, NvGender gender, String nameGroup) {
		List<TextureMetadata> all = SERVER_CATEGORIES.getOrDefault(type, List.of());
		if (all.isEmpty()) {
			return switch (type) {
				case "clothes" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "clothes_female_default");
				case "hair" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "hair_female_default");
				case "face" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "face_female_default");
				default -> Identifier.fromNamespaceAndPath(Villagium.MODID, "skin_female_default");
			};
		}
		
		List<TextureMetadata> filtered = new ArrayList<>();
		for (TextureMetadata def : all) {
			if (!def.gender().equals("ANY") && !def.gender().equals(gender.name()))
				continue;
			if (!nameGroup.equalsIgnoreCase("generic") && !def.cultures().isEmpty() && !def.cultures().contains(nameGroup.toLowerCase(Locale.ROOT)))
				continue;
			filtered.add(def);
		}
		
		if (filtered.isEmpty()) {
			for (TextureMetadata def : all) {
				if (def.gender().equals("ANY") || def.gender().equals(gender.name()))
					filtered.add(def);
			}
		}
		
		if (filtered.isEmpty() && !all.isEmpty())
			return all.get(RND.nextInt(all.size())).id();
		if (filtered.isEmpty()) {
			return switch (type) {
				case "clothes" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "clothes_female_default");
				case "hair" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "hair_female_default");
				case "face" -> Identifier.fromNamespaceAndPath(Villagium.MODID, "face_female_default");
				default -> Identifier.fromNamespaceAndPath(Villagium.MODID, "skin_female_default");
			};
		}
		
		return filtered.get(RND.nextInt(filtered.size())).id();
	}
	
	public static Identifier getTexturePath(Identifier id) {
		Identifier path = CLIENT_TEXTURES.get(id);
		return path == null ? MISSING_TEXTURE : path;
	}
}
