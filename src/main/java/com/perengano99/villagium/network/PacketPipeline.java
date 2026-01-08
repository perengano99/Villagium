package com.perengano99.villagium.network;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class PacketPipeline {
	
	private PayloadRegistrar payload = null;
	
	public PacketPipeline() {}
	
	void loadRegistar(PayloadRegistrar payload){
		if (this.payload == null) this.payload = payload;
		else throw new IllegalThreadStateException("The PayloadRegistrar has already been loaded.");
	}
	
	public <T extends IPayloadPacket<T>> void sendToServer(IPayloadPacket<T> packet) {
		Preconditions.checkState(FMLEnvironment.getDist().isClient(), "Cannot send serverbound payloads on the server");
		ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
		listener.send(packet);
	}
	
	public <T extends IPayloadPacket<T>> void sendToClient(ServerPlayer player, IPayloadPacket<T> packet) {
		player.connection.send(new ClientboundCustomPayloadPacket(packet));
	}
	
	public <T extends IPayloadPacket<T>> void sendToTracking(Entity target, IPayloadPacket<T> packet) {
		PacketDistributor.sendToPlayersTrackingEntity(target, packet);
	}
	
	public <T extends IPayloadPacket<T>> void registerToPacket(Class<T> clazz, Dist dist) {
		if (payload == null) throw new IllegalStateException("PayloadRegistrar has not been loaded. Call loadRegistrar() first.");
		
		final StreamDecoder<FriendlyByteBuf, T> packetDecoder = getByteBufTStreamDecoder(clazz);
		final StreamMemberEncoder<FriendlyByteBuf, T> packetEncoder = IPayloadPacket::encode;
		final IPayloadHandler<T> packetHandler = (packetInstance, context) -> packetInstance.handle(packetInstance, context);
		
		if (dist.isClient())
			payload.playToClient(NetworkManager.getPacketType(clazz), StreamCodec.ofMember(packetEncoder, packetDecoder), packetHandler);
		else
			payload.playToServer(NetworkManager.getPacketType(clazz), StreamCodec.ofMember(packetEncoder, packetDecoder), packetHandler);
	}
	
	private static <T extends IPayloadPacket<T>> @NotNull StreamDecoder<FriendlyByteBuf, T> getByteBufTStreamDecoder(Class<T> clazz) {
		final StreamDecoder<FriendlyByteBuf, T> packetDecoder;
		try {
			final Constructor<T> constructorRef = clazz.getConstructor(FriendlyByteBuf.class);
			packetDecoder = buf -> {
				try {
					return constructorRef.newInstance(buf);
				} catch (Exception e) {
					throw new RuntimeException("Failed to instantiate packet " + clazz.getSimpleName() + " from ByteBuf", e);
				}
			};
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Packet class " + clazz.getSimpleName() + " must have a public constructor that accepts a single ByteBuf argument.", e);
		}
		return packetDecoder;
	}
	
}
