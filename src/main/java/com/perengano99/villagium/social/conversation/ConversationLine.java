package com.perengano99.villagium.social.conversation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import java.util.List;
import java.util.Optional;

public record ConversationLine(
		List<String> translationKeys,
		Optional<JsonObject> conditions,
		Optional<String> speaker
) {
	public static final Codec<ConversationLine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("keys").forGetter(ConversationLine::translationKeys),
			ExtraCodecs.JSON.xmap(JsonElement::getAsJsonObject, el -> el).optionalFieldOf("conditions").forGetter(ConversationLine::conditions),
			Codec.STRING.optionalFieldOf("speaker").forGetter(ConversationLine::speaker)
	).apply(instance, ConversationLine::new));
}
