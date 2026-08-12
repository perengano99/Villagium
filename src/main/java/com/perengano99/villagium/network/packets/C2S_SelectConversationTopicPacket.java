package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.conversation.ConversationSession;
import com.perengano99.villagium.social.conversation.ServerConversationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_SelectConversationTopicPacket(int entityId, Identifier topicId) implements IPayloadPacket<C2S_SelectConversationTopicPacket> {
	public C2S_SelectConversationTopicPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), Identifier.STREAM_CODEC.decode(buf));
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		Identifier.STREAM_CODEC.encode(buf, topicId);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_SelectConversationTopicPacket.class);
	}

	@Override
	public void handle(@NotNull C2S_SelectConversationTopicPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				ConversationSession session = ServerConversationManager.getSession(player.getUUID());
				if (session != null)
					session.selectTopic(packet.topicId());
			}
		});
	}
}
