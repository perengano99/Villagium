package com.perengano99.villagium.social.conversation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import java.util.Map;
import java.util.Optional;

public record ConversationCancellationEffects(
		Optional<Map<RelationshipAxis, Float>> relationshipEffects,
		float fatigueCost
) {
	public static final Codec<ConversationCancellationEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(RelationshipAxis.CODEC, Codec.FLOAT).optionalFieldOf("relationship_effects").forGetter(ConversationCancellationEffects::relationshipEffects),
			Codec.FLOAT.optionalFieldOf("fatigue_cost", 0.0f).forGetter(ConversationCancellationEffects::fatigueCost)
	).apply(instance, ConversationCancellationEffects::new));
}
