package com.perengano99.villagium.network;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.network.packets.PingPongPacket;
import com.perengano99.villagium.network.packets.SyncMobAnimationPacket;
import com.perengano99.villagium.network.packets.SyncRegisteredAnimationsToServerPacket;
import com.perengano99.villagium.network.packets.SyncAppearanceToClientPacket;
import com.perengano99.villagium.network.packets.C2S_TriggerSocialEventPacket;
import com.perengano99.villagium.network.packets.S2C_UpdateDialoguePacket;
import com.perengano99.villagium.network.packets.S2C_SpawnRelationshipAxisParticlePacket;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.network.packets.C2S_CloseInteractionPacket;
import com.perengano99.villagium.network.packets.C2S_StartConversationPacket;
import com.perengano99.villagium.network.packets.C2S_SelectConversationChoicePacket;
import com.perengano99.villagium.network.packets.S2C_UpdateConversationPacket;
import com.perengano99.villagium.network.packets.C2S_SelectConversationCategoryPacket;
import com.perengano99.villagium.network.packets.C2S_SelectConversationTopicPacket;
import com.perengano99.villagium.network.packets.C2S_CancelConversationPacket;
import com.perengano99.villagium.network.packets.server.OpenNpcMenuPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Villagium.MODID)
public class NetworkManager {
	private static final String PROTOCOL_V = "1.1";
	public static final PacketPipeline PIPELINE = new PacketPipeline();
	
	@SubscribeEvent
	public static void registerPacketPipeline(final RegisterPayloadHandlersEvent event){
		PIPELINE.loadRegistar(event.registrar(PROTOCOL_V));
		
		PIPELINE.registerToPacket(PingPongPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(SyncMobAnimationPacket.class, Dist.CLIENT);
		PIPELINE.registerToPacket(SyncRegisteredAnimationsToServerPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(SyncAppearanceToClientPacket.class, Dist.CLIENT);
		PIPELINE.registerToPacket(C2S_TriggerSocialEventPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(S2C_UpdateDialoguePacket.class, Dist.CLIENT);
		PIPELINE.registerToPacket(S2C_SpawnRelationshipAxisParticlePacket.class, Dist.CLIENT);
		
		//Sv
		PIPELINE.registerToPacket(OpenNpcMenuPacket.class, Dist.CLIENT);
		
		
//		PIPELINE.registerToPacket(S2C_OpenInteractMenuPacket.class, Dist.CLIENT);
		
		
		PIPELINE.registerToPacket(C2S_CloseInteractionPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(C2S_StartConversationPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(C2S_SelectConversationChoicePacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(S2C_UpdateConversationPacket.class, Dist.CLIENT);
		PIPELINE.registerToPacket(C2S_SelectConversationCategoryPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(C2S_SelectConversationTopicPacket.class, Dist.DEDICATED_SERVER);
		PIPELINE.registerToPacket(C2S_CancelConversationPacket.class, Dist.DEDICATED_SERVER);
	}
	
	public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getPacketType(Class<T> clazz) {
		return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Villagium.MODID, clazz.getSimpleName().toLowerCase()));
	}
}
