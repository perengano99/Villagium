package com.perengano99.villagium.network;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.network.packets.PingPongPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ResourceBundle;

@EventBusSubscriber(modid = Villagium.MODID)
public class NetworkManager {
	private static final String PROTOCOL_V = "1.1";
	public static final PacketPipeline PIPELINE = new PacketPipeline();
	
	@SubscribeEvent
	public static void registerPacketPipeline(final RegisterPayloadHandlersEvent event){
		PIPELINE.loadRegistar(event.registrar(PROTOCOL_V));
		
		PIPELINE.registerToPacket(PingPongPacket.class, Dist.DEDICATED_SERVER);
	}
	
	public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getPacketType(Class<T> clazz) {
		return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Villagium.MODID, clazz.getSimpleName().toLowerCase()));
	}
}
