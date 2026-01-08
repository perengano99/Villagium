package com.perengano99.villagium.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public interface IPayloadPacket<T extends IPayloadPacket> extends CustomPacketPayload, IPayloadHandler<T> {
	void encode(FriendlyByteBuf buffer);
	
	// Extiende
	// 'Type<? extends CustomPacketPayload> type();' de CustomPacketPayload
	
	// Extiende
	// 'void handle(T packet, IPayloadContext context);' de IPayloadHandler<T>
}