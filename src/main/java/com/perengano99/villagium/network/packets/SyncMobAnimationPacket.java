package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record SyncMobAnimationPacket(
	int entityId,
	boolean start,
	String animationId,
	byte loopMode, // 0=false, 1=true, 2=default
	int durationTicks,
	float speedFactor
) implements IPayloadPacket<SyncMobAnimationPacket> {

	public SyncMobAnimationPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), buf.readBoolean(), buf.readUtf(), buf.readByte(), buf.readVarInt(), buf.readFloat());
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeBoolean(start);
		buf.writeUtf(animationId);
		buf.writeByte(loopMode);
		buf.writeVarInt(durationTicks);
		buf.writeFloat(speedFactor);
	}

	@Override
	public @NonNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(SyncMobAnimationPacket.class);
	}

	@Override
	public void handle(SyncMobAnimationPacket payload, @NonNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isClientbound()) {
				com.perengano99.villagium.client.network.ClientPacketHandler.handleSyncMobAnimation(payload);
			}
		});
	}
}
