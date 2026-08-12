package com.perengano99.villagium.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public interface IPayloadPacket<T extends IPayloadPacket<T>> extends CustomPacketPayload, IPayloadHandler<T> {
	default void encode(FriendlyByteBuf buffer) {
		throw new UnsupportedOperationException("Uses StreamCodec.");
	}
	
	// Extiende
	// 'Type<? extends CustomPacketPayload> type();' de CustomPacketPayload
	
	// Extiende
	// 'void handle(T packet, IPayloadContext context);' de IPayloadHandler<T>
}