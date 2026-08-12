package com.perengano99.villagium.network.packets.server;

import com.perengano99.villagium.client.network.ClientPacketHandler;
import com.perengano99.villagium.client.registration.NpcMenuRegistry.MenuDataPacket;
import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.profile.ProfileData;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


public record OpenNpcMenuPacket(int entityId, ProfileData profileData, RelationshipData.ClientData relationData
) implements IPayloadPacket<OpenNpcMenuPacket>, MenuDataPacket {
	
	public static final StreamCodec<FriendlyByteBuf, OpenNpcMenuPacket> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull OpenNpcMenuPacket decode(FriendlyByteBuf buf) {
			return new OpenNpcMenuPacket(
					buf.readVarInt(),
					ProfileData.STREAM_CODEC.decode(buf),
					RelationshipData.ClientData.STREAM_CODEC.decode(buf)
			);
		}
		
		@Override
		public void encode(FriendlyByteBuf buf, OpenNpcMenuPacket packet) {
			buf.writeVarInt(packet.entityId());
			ProfileData.STREAM_CODEC.encode(buf, packet.profileData());
			RelationshipData.ClientData.STREAM_CODEC.encode(buf, packet.relationData());
		}
	};
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(OpenNpcMenuPacket.class);
	}
	
	@Override
	public void handle(@NotNull OpenNpcMenuPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isClientbound())
				ClientPacketHandler.handleOpenInteractionScreen(packet);
		});
	}
}
