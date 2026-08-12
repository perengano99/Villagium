package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.conversation.ServerConversationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_StartConversationPacket(int entityId) implements IPayloadPacket<C2S_StartConversationPacket> {
	public C2S_StartConversationPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_StartConversationPacket.class);
	}

	@Override
	public void handle(@NotNull C2S_StartConversationPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				ServerLevel level = (ServerLevel) player.level();
				Entity entity = level.getEntity(packet.entityId());
				if (entity instanceof VillagiumMob<?> npc)
					if (player.distanceToSqr(npc) <= 64.0)
						ServerConversationManager.startConversation(player, npc);
			}
		});
	}
}
