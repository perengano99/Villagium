package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_CloseInteractionPacket(int entityId) implements IPayloadPacket<C2S_CloseInteractionPacket> {
	
	public C2S_CloseInteractionPacket(FriendlyByteBuf buffer) {
		this(buffer.readInt());
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
	}
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_CloseInteractionPacket.class);
	}
	
	@Override
	public void handle(@NotNull C2S_CloseInteractionPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				Entity entity = player.level().getEntity(packet.entityId());
				if (entity instanceof VillagiumMob<?> npc)
					if (player.equals(npc.interactingPlayer))
						npc.interactingPlayer = null;
			}
		});
	}
}
