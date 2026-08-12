package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.conversation.ConversationSession;
import com.perengano99.villagium.social.conversation.ServerConversationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_CancelConversationPacket(int entityId) implements IPayloadPacket<C2S_CancelConversationPacket> {
	public C2S_CancelConversationPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_CancelConversationPacket.class);
	}

	@Override
	public void handle(@NotNull C2S_CancelConversationPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				ConversationSession session = ServerConversationManager.getSession(player.getUUID());
				if (session != null)
					session.cancelConversation();
			}
		});
	}
}
