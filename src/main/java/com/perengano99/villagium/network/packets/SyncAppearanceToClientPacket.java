package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.profile.AppearanceData;
import com.perengano99.villagium.social.profile.ProfileData;
import com.perengano99.villagium.core.registration.ModAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record SyncAppearanceToClientPacket(
		int entityId,
		ProfileData profileData
) implements IPayloadPacket<SyncAppearanceToClientPacket> {

	public SyncAppearanceToClientPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), ProfileData.STREAM_CODEC.decode(buf));
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		ProfileData.STREAM_CODEC.encode(buf, profileData);
	}

	@Override
	public @NonNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(SyncAppearanceToClientPacket.class);
	}

	@Override
	public void handle(SyncAppearanceToClientPacket payload, @NonNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isClientbound())
				com.perengano99.villagium.client.network.ClientPacketHandler.handleSyncAppearance(payload);
		});
	}
}
