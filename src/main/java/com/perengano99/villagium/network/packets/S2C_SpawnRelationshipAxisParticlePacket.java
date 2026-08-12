package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2C_SpawnRelationshipAxisParticlePacket(int entityId, int atlasIndex) implements IPayloadPacket<S2C_SpawnRelationshipAxisParticlePacket> {
	public S2C_SpawnRelationshipAxisParticlePacket(FriendlyByteBuf buffer) {
		this(buffer.readInt(), buffer.readInt());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeInt(atlasIndex);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(S2C_SpawnRelationshipAxisParticlePacket.class);
	}

	@Override
	public void handle(@NotNull S2C_SpawnRelationshipAxisParticlePacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			// Trigger particles on client side tracking entity
		});
	}
}
