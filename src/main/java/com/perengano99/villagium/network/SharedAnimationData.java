package com.perengano99.villagium.network;

import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.network.packets.SyncRegisteredAnimationsToServerPacket;

import java.util.*;

public class SharedAnimationData {
	private static final Map<AnimationCategory, List<String>> CATEGORY_TO_IDS = new EnumMap<>(AnimationCategory.class);
	private static final List<String> ALL_IDS = new ArrayList<>();

	static {
		for (AnimationCategory category : AnimationCategory.values()) {
			CATEGORY_TO_IDS.put(category, new ArrayList<>());
		}
	}

	public static synchronized void registerId(String id, AnimationCategory category) {
		List<String> list = CATEGORY_TO_IDS.get(category);
		if (!list.contains(id)) {
			list.add(id);
		}
		if (!ALL_IDS.contains(id)) {
			ALL_IDS.add(id);
		}
	}

	public static synchronized List<String> getIdsForCategory(String categoryName) {
		try {
			AnimationCategory category = AnimationCategory.valueOf(categoryName.toUpperCase(Locale.ROOT));
			return new ArrayList<>(CATEGORY_TO_IDS.getOrDefault(category, List.of()));
		} catch (IllegalArgumentException e) {
			return List.of();
		}
	}

	public static synchronized List<String> getAllIds() {
		return new ArrayList<>(ALL_IDS);
	}

	public static synchronized List<SyncRegisteredAnimationsToServerPacket.AnimationEntry> getEntries() {
		List<SyncRegisteredAnimationsToServerPacket.AnimationEntry> list = new ArrayList<>();
		for (Map.Entry<AnimationCategory, List<String>> entry : CATEGORY_TO_IDS.entrySet()) {
			for (String id : entry.getValue()) {
				list.add(new SyncRegisteredAnimationsToServerPacket.AnimationEntry(id, entry.getKey()));
			}
		}
		return list;
	}
}
