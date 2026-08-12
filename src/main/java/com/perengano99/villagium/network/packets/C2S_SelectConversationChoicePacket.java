package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.conversation.ConversationSession;
import com.perengano99.villagium.social.conversation.ServerConversationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_SelectConversationChoicePacket(int entityId, int choiceIndex) implements IPayloadPacket<C2S_SelectConversationChoicePacket> {
	public C2S_SelectConversationChoicePacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), buf.readVarInt());
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeVarInt(choiceIndex);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_SelectConversationChoicePacket.class);
	}

	@Override
	public void handle(@NotNull C2S_SelectConversationChoicePacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
				ConversationSession session = ServerConversationManager.getSession(player.getUUID());
				if (session != null)
					session.selectChoice(packet.choiceIndex());
			}
		});
	}
}
