package com.perengano99.villagium.social.conversation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record ConversationNode(
		String id,
		List<ConversationLine> lines,
		List<ConversationChoice> choices
) {
	public static final Codec<ConversationNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(ConversationNode::id),
			ConversationLine.CODEC.listOf().fieldOf("lines").forGetter(ConversationNode::lines),
			ConversationChoice.CODEC.listOf().optionalFieldOf("choices", List.of()).forGetter(ConversationNode::choices)
	).apply(instance, ConversationNode::new));
}
