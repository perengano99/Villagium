package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record PingPongPacket() implements IPayloadPacket<PingPongPacket> {
	
	public PingPongPacket(FriendlyByteBuf buffer) {this();}
	
	@Override
	public void encode(FriendlyByteBuf buffer) {
	}
	
	@Override
	public @NonNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(PingPongPacket.class);
	}
	
	@Override
	public void handle(PingPongPacket payload, @NonNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player){
				player.displayClientMessage(Component.literal("Pong!"), false);
			}
		});
	}
}
