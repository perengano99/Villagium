package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public record S2C_UpdateConversationPacket(
		int entityId,
		Identifier topicId,
		String nodeId,
		Component villagerText,
		List<ChoiceClientData> choices
) implements IPayloadPacket<S2C_UpdateConversationPacket> {

	public S2C_UpdateConversationPacket(FriendlyByteBuf buf) {
		this(
				buf.readVarInt(),
				Identifier.STREAM_CODEC.decode(buf),
				buf.readUtf(),
				ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf),
				buf.readCollection(ArrayList::new, ChoiceClientData::new)
		);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		Identifier.STREAM_CODEC.encode(buf, topicId);
		buf.writeUtf(nodeId);
		ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, villagerText);
		buf.writeCollection(choices, (b, choice) -> choice.encode(b));
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(S2C_UpdateConversationPacket.class);
	}

	@Override
	public void handle(@NotNull S2C_UpdateConversationPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isClientbound())
				com.perengano99.villagium.client.network.ClientPacketHandler.handleUpdateConversation(packet);
		});
	}

	public record ChoiceClientData(int index, String textKey, boolean available) {
		public ChoiceClientData(FriendlyByteBuf buf) {
			this(buf.readVarInt(), buf.readUtf(), buf.readBoolean());
		}

		public void encode(FriendlyByteBuf buf) {
			buf.writeVarInt(index);
			buf.writeUtf(textKey);
			buf.writeBoolean(available);
		}
	}
}
