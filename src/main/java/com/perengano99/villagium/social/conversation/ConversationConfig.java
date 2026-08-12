package com.perengano99.villagium.social.conversation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public record ConversationConfig(
		Map<String, ConversationNode> nodes,
		String startNodeId
) {
	public static final Codec<ConversationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, ConversationNode.CODEC).fieldOf("nodes").forGetter(ConversationConfig::nodes),
			Codec.STRING.fieldOf("start_node").forGetter(ConversationConfig::startNodeId)
	).apply(instance, ConversationConfig::new));
}
