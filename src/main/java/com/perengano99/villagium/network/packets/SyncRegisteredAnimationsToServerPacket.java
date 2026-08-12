package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.SharedAnimationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record SyncRegisteredAnimationsToServerPacket(List<AnimationEntry> entries) implements IPayloadPacket<SyncRegisteredAnimationsToServerPacket> {

	public record AnimationEntry(String id, AnimationCategory category) {}

	public SyncRegisteredAnimationsToServerPacket(FriendlyByteBuf buf) {
		this(readEntries(buf));
	}

	private static List<AnimationEntry> readEntries(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<AnimationEntry> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(new AnimationEntry(buf.readUtf(), buf.readEnum(AnimationCategory.class)));
		}
		return list;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entries.size());
		for (AnimationEntry entry : entries) {
			buf.writeUtf(entry.id());
			buf.writeEnum(entry.category());
		}
	}

	@Override
	public @NonNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(SyncRegisteredAnimationsToServerPacket.class);
	}

	@Override
	public void handle(SyncRegisteredAnimationsToServerPacket payload, @NonNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.flow().isServerbound()) {
				for (AnimationEntry entry : payload.entries()) {
					SharedAnimationData.registerId(entry.id(), entry.category());
				}
			}
		});
	}
}
