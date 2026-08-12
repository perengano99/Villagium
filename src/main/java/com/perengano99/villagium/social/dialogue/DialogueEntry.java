package com.perengano99.villagium.social.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DialogueEntry(
		String id,
		Identifier personalityId,
		String type,
		Optional<String> event,
		Optional<JsonObject> conditions,
		List<String> translationKeys,
		int weight,
		int specificity
) {
	public static final Codec<DialogueEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(DialogueEntry::id),
			Identifier.CODEC.optionalFieldOf("personality_id", Identifier.fromNamespaceAndPath("villagium", "generic")).forGetter(DialogueEntry::personalityId),
			Codec.STRING.fieldOf("type").forGetter(DialogueEntry::type),
			Codec.STRING.optionalFieldOf("event").forGetter(DialogueEntry::event),
			ExtraCodecs.JSON.xmap(JsonElement::getAsJsonObject, el -> el).optionalFieldOf("conditions").forGetter(DialogueEntry::conditions),
			Codec.STRING.listOf().fieldOf("keys").forGetter(DialogueEntry::translationKeys),
			Codec.INT.fieldOf("weight").orElse(1).forGetter(DialogueEntry::weight),
			Codec.INT.optionalFieldOf("specificity", 0).forGetter(DialogueEntry::specificity)
	).apply(instance, DialogueEntry::new));

	public DialogueEntry(String id, Identifier personalityId, String type, Optional<String> event, Optional<JsonObject> conditions, List<String> translationKeys, int weight) {
		this(id, personalityId, type, event, conditions, translationKeys, weight, conditions.map(DialogueEntry::calculateSpecificity).orElse(0));
	}

	private static int calculateSpecificity(JsonObject conditions) {
		if (conditions == null)
			return 0;

		int score = conditions.size();
		for (Map.Entry<String, JsonElement> entry : conditions.entrySet())
			if (entry.getValue().isJsonObject())
				score++;
		return score;
	}
}
