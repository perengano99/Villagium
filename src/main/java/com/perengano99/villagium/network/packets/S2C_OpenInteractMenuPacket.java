package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.client.network.ClientPacketHandler;
import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// packet sent to client to open interaction GUI.
public record S2C_OpenInteractMenuPacket(
		int entityId,
		ProfileData profileData,
		List<Identifier> activePlayerTags
) implements IPayloadPacket<S2C_OpenInteractMenuPacket> {
	
	public static final StreamCodec<FriendlyByteBuf, S2C_OpenInteractMenuPacket> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull S2C_OpenInteractMenuPacket decode(FriendlyByteBuf buf) {
			return new S2C_OpenInteractMenuPacket(
					buf.readVarInt(),
					ProfileData.STREAM_CODEC.decode(buf),
					buf.readCollection(ArrayList::new, Identifier.STREAM_CODEC)
			);
		}
		
		@Override
		public void encode(FriendlyByteBuf buf, S2C_OpenInteractMenuPacket packet) {
			buf.writeVarInt(packet.entityId());
			ProfileData.STREAM_CODEC.encode(buf, packet.profileData());
			buf.writeCollection(packet.activePlayerTags(), Identifier.STREAM_CODEC);
		}
	};
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(S2C_OpenInteractMenuPacket.class);
	}
	
	@Override
	public void handle(@NotNull S2C_OpenInteractMenuPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isClientbound())
				ClientPacketHandler.handleOpenInteractionScreen(packet);
		});
	}
}
