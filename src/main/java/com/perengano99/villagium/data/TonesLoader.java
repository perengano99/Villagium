package com.perengano99.villagium.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TonesLoader extends SimpleJsonResourceReloadListener<JsonElement> {
	
	private static final Logger LOGGER = Logger.getLogger();
	private static final Random RND = new Random();
	
	public static final int MISSING_COLOR = 0xFFFF00FF; // Magenta
	public static final ToneColorEntry MISSING_COLOR_ENTRY = new ToneColorEntry(MISSING_COLOR, MISSING_COLOR, MISSING_COLOR, 1);

	public record ToneMetadata(int size, List<String> cultures) {}
	public record ToneColorEntry(int skin, int hair, int eye, int weight) {}
	
	private static final Map<String, ToneMetadata> SERVER_GROUPS = new ConcurrentHashMap<>();
	private static final Map<String, List<ToneColorEntry>> CLIENT_GROUPS = new ConcurrentHashMap<>();
	
	public TonesLoader() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("nv_tones"));
	}
	
	@Override
	protected void apply(@NotNull Map<Identifier, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		boolean hasMetadata = resources.values().stream().anyMatch(el -> el.isJsonObject() && el.getAsJsonObject().has("metadata"));
		boolean hasColors = resources.values().stream().anyMatch(el -> el.isJsonObject() && el.getAsJsonObject().has("colors"));
		
		if (hasMetadata)
			SERVER_GROUPS.clear();
		if (hasColors)
			CLIENT_GROUPS.clear();
		
		LOGGER.info("Loading skin tone groups (unified)...");
		
		resources.forEach((location, element) -> {
			if (!element.isJsonObject())
				return;
			
			JsonObject obj = element.getAsJsonObject();
			String groupName = location.getPath().toLowerCase(Locale.ROOT);
			
			if (obj.has("metadata") && obj.get("metadata").isJsonObject()) {
				JsonObject metaObj = obj.getAsJsonObject("metadata");
				int size = metaObj.has("size") ? metaObj.get("size").getAsInt() : 1;
				List<String> cultures = new ArrayList<>();
				if (metaObj.has("cultures") && metaObj.get("cultures").isJsonArray()) {
					for (JsonElement c : metaObj.getAsJsonArray("cultures"))
						cultures.add(c.getAsString());
				}
				SERVER_GROUPS.put(groupName, new ToneMetadata(size, cultures));
			}
			
			if (obj.has("colors") && obj.get("colors").isJsonArray()) {
				JsonArray colorsArr = obj.getAsJsonArray("colors");
				List<ToneColorEntry> colorsList = new ArrayList<>();
				for (JsonElement cElem : colorsArr) {
					if (cElem.isJsonObject()) {
						JsonObject cObj = cElem.getAsJsonObject();
						int skin = parseColorElement(cObj.get("skin"));
						int hair = parseColorElement(cObj.get("hair"));
						int eye = parseColorElement(cObj.get("eye"));
						int weight = cObj.has("weight") ? cObj.get("weight").getAsInt() : 1;
						colorsList.add(new ToneColorEntry(skin, hair, eye, weight));
					}
				}
				CLIENT_GROUPS.put(groupName, colorsList);
			}
		});
		
		LOGGER.info("Loaded {} server metadata groups, {} client color groups.", SERVER_GROUPS.size(), CLIENT_GROUPS.size());
	}
	
	private static int parseColorElement(JsonElement element) {
		if (element == null)
			return MISSING_COLOR;
		if (element.isJsonPrimitive()) {
			var prim = element.getAsJsonPrimitive();
			if (prim.isNumber())
				return prim.getAsInt();
			if (prim.isString()) {
				String str = prim.getAsString();
				try {
					if (str.startsWith("0x") || str.startsWith("0X"))
						return (int) Long.parseLong(str.substring(2), 16);
					if (str.startsWith("#"))
						return (int) Long.parseLong(str.substring(1), 16);
					return (int) Long.parseLong(str, 16);
				} catch (NumberFormatException e) {
					LOGGER.warn("Invalid hex color format: " + str + ", using missing_color.");
					return MISSING_COLOR;
				}
			}
		}
		return MISSING_COLOR;
	}
	
	public record ServerToneResult(Identifier group, int index) {}
	
	public static ServerToneResult getRandomToneIndex(String culture) {
		List<String> matching = new ArrayList<>();
		SERVER_GROUPS.forEach((name, meta) -> {
			if (meta.cultures().contains(culture.toLowerCase(Locale.ROOT)))
				matching.add(name);
		});
		if (matching.isEmpty()) {
			if (SERVER_GROUPS.containsKey("generic"))
				matching.add("generic");
		}
		
		String group = matching.isEmpty() ? "generic" : matching.get(RND.nextInt(matching.size()));
		ToneMetadata meta = SERVER_GROUPS.get(group);
		int maxIndex = meta != null ? meta.size() : 1;
		int index = RND.nextInt(maxIndex);
		
		return new ServerToneResult(Identifier.fromNamespaceAndPath(Villagium.MODID, group), index);
	}
	
	public static ToneColorEntry getColors(Identifier groupId, int index) {
		String name = groupId.getPath().toLowerCase(Locale.ROOT);
		List<ToneColorEntry> list = CLIENT_GROUPS.get(name);
		if (list == null || list.isEmpty())
			return MISSING_COLOR_ENTRY;
		
		int actualIndex = Math.floorMod(index, list.size());
		return list.get(actualIndex);
	}
}
