package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2C_UpdateDialoguePacket(String textKey) implements IPayloadPacket<S2C_UpdateDialoguePacket> {
	public S2C_UpdateDialoguePacket(FriendlyByteBuf buffer) {
		this(buffer.readUtf());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUtf(textKey);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(S2C_UpdateDialoguePacket.class);
	}

	@Override
	public void handle(@NotNull S2C_UpdateDialoguePacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			// Update local dialog UI when GUI screen is added.
		});
	}
}
