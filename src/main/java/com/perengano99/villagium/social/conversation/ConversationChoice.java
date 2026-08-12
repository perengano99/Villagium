package com.perengano99.villagium.social.conversation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ConversationChoice(
		String textKey,
		Optional<String> nextNodeId,
		Optional<JsonObject> conditions,
		Optional<Map<RelationshipAxis, Float>> relationshipEffects,
		Optional<String> forceMoodId,
		Optional<Map<String, Float>> moodModifiers,
		Optional<List<String>> addTags,
		Optional<List<String>> removeTags,
		float fatigueCost
) {
	public static final Codec<ConversationChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("text").forGetter(ConversationChoice::textKey),
			Codec.STRING.optionalFieldOf("next_node").forGetter(ConversationChoice::nextNodeId),
			ExtraCodecs.JSON.xmap(JsonElement::getAsJsonObject, el -> el).optionalFieldOf("conditions").forGetter(ConversationChoice::conditions),
			Codec.unboundedMap(RelationshipAxis.CODEC, Codec.FLOAT).optionalFieldOf("relationship_effects").forGetter(ConversationChoice::relationshipEffects),
			Codec.STRING.optionalFieldOf("force_mood").forGetter(ConversationChoice::forceMoodId),
			Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("mood_modifiers").forGetter(ConversationChoice::moodModifiers),
			Codec.STRING.listOf().optionalFieldOf("add_tags").forGetter(ConversationChoice::addTags),
			Codec.STRING.listOf().optionalFieldOf("remove_tags").forGetter(ConversationChoice::removeTags),
			Codec.FLOAT.optionalFieldOf("fatigue_cost", 0.0f).forGetter(ConversationChoice::fatigueCost)
	).apply(instance, ConversationChoice::new));
}
