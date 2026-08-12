package com.perengano99.villagium.network.packets;

import com.perengano99.villagium.network.IPayloadPacket;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.SocialEventManager;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2S_TriggerSocialEventPacket(int entityId, Identifier eventId, CompoundTag clientContextData) implements IPayloadPacket<C2S_TriggerSocialEventPacket> {
	public C2S_TriggerSocialEventPacket(FriendlyByteBuf buffer) {
		this(buffer.readInt(), Identifier.STREAM_CODEC.decode(buffer), buffer.readNbt());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		Identifier.STREAM_CODEC.encode(buffer, eventId);
		buffer.writeNbt(clientContextData);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return NetworkManager.getPacketType(C2S_TriggerSocialEventPacket.class);
	}

	@Override
	public void handle(@NotNull C2S_TriggerSocialEventPacket packet, @NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				ServerLevel serverLevel = (ServerLevel) player.level();
				Entity entity = serverLevel.getEntity(packet.entityId());
				if (entity instanceof VillagiumMob<?> npc) {
					if (npc.interactingPlayer != null && !player.equals(npc.interactingPlayer))
						return;
					if (player.distanceToSqr(npc) > 64.0)
						return;

					SocialEventContext serverContext = new SocialEventContext();
					serverContext.put(ContextKeys.PLAYER, player);
					serverContext.put(ContextKeys.VILLAGER, npc);

					SocialEventDefinition eventDef = VillagiumData.getSocialEvent(packet.eventId());
					if (eventDef != null)
						SocialEventManager.handleTrigger(eventDef, serverContext);
				}
			}
		});
	}
}
