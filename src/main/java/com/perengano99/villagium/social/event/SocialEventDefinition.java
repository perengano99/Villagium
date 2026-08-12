package com.perengano99.villagium.social.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.conversation.ConversationCancellationEffects;
import com.perengano99.villagium.social.conversation.ConversationConfig;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record SocialEventDefinition(
		Identifier id,
		SocialEvent type,
		Set<String> categories,
		Optional<Map<Identifier, SocialEventDefinition>> variants,
		Optional<Identifier> parentId,
		int baseImpact,
		Map<RelationshipAxis, Float> affinityAxes,
		Optional<JsonObject> conditions,
		Optional<String> displayKey,
		Optional<Identifier> icon,
		Optional<ConversationConfig> conversationConfig,
		Optional<ConversationCancellationEffects> cancellationEffects
) {
	private static final com.perengano99.villagium.core.util.logging.Logger LOGGER = com.perengano99.villagium.core.util.logging.Logger.getLogger();

	public static final Codec<SocialEventDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(SocialEventDefinition::id),
			Identifier.CODEC.flatXmap(
					id -> {
						SocialEvent event = SocialEventRegistry.get(id);
						if (event == null) {
							LOGGER.warn("Unknown SocialEvent type '{}', using conversation fallback.", id);
							event = SocialEventRegistry.get(Identifier.fromNamespaceAndPath("villagium", "conversation"));
						}
						return event != null ? DataResult.success(event) : DataResult.error(() -> "Critical: Conversation fallback event not registered!");
					},
					event -> DataResult.success(event.id())
			).fieldOf("type").forGetter(SocialEventDefinition::type),
			Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("categories", Set.of()).forGetter(SocialEventDefinition::categories),
			Codec.lazyInitialized(() -> Codec.unboundedMap(Identifier.CODEC, SocialEventDefinition.CODEC)).optionalFieldOf("variants").forGetter(SocialEventDefinition::variants),
			Identifier.CODEC.optionalFieldOf("parent_id").forGetter(SocialEventDefinition::parentId),
			Codec.INT.optionalFieldOf("base_impact", 0).forGetter(SocialEventDefinition::baseImpact),
			Codec.unboundedMap(RelationshipAxis.CODEC, Codec.FLOAT).optionalFieldOf("affinity_impact", Map.of()).forGetter(SocialEventDefinition::affinityAxes),
			ExtraCodecs.JSON.xmap(JsonElement::getAsJsonObject, el -> el).optionalFieldOf("conditions").forGetter(SocialEventDefinition::conditions),
			Codec.STRING.optionalFieldOf("display_key").forGetter(SocialEventDefinition::displayKey),
			Identifier.CODEC.optionalFieldOf("icon").forGetter(SocialEventDefinition::icon),
			ConversationConfig.CODEC.optionalFieldOf("conversation_config").forGetter(SocialEventDefinition::conversationConfig),
			ConversationCancellationEffects.CODEC.optionalFieldOf("cancellation_effects").forGetter(SocialEventDefinition::cancellationEffects)
	).apply(instance, SocialEventDefinition::new));
}
