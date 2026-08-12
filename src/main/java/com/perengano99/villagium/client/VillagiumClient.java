package com.perengano99.villagium.client;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.gui.screens.NpcMenuScreen;
import com.perengano99.villagium.client.gui.screens.VillagerInteractionScreen;
import com.perengano99.villagium.client.gui.screens.VillagerNpcMenuScreen;
import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.animation.TempAnimManager;
import com.perengano99.villagium.client.registration.NpcMenuRegistry;
import com.perengano99.villagium.client.renderer.entity.NvVillagerRenderer;
import com.perengano99.villagium.core.registration.ModEntityTypes;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.data.AppearanceLoader;
import com.perengano99.villagium.data.TonesLoader;
import com.perengano99.villagium.entity.npc.NvVillager;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.SharedAnimationData;
import com.perengano99.villagium.network.packets.SyncRegisteredAnimationsToServerPacket;
import com.perengano99.villagium.network.packets.server.OpenNpcMenuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.perengano99.villagium.entity.VillagiumMob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = Villagium.MODID, value = Dist.CLIENT)
public final class VillagiumClient {
	
	private static final Logger LOGGER = Logger.getLogger();
	
	private static final ResourcesReloadListener RELOAD_LISTENER = new ResourcesReloadListener();
	private static final TonesLoader TONES_LOADER = new TonesLoader();
	private static final AppearanceLoader APPEARANCE_LOADER = new AppearanceLoader();
	
	public static VillagiumMob<?> activeInteractionMob;
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		NpcMenuRegistry.register(ModEntityTypes.NV_VILLAGER.get(), payload -> new VillagerNpcMenuScreen((OpenNpcMenuPacket) payload));
	}
	
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.NV_VILLAGER.get(), NvVillagerRenderer::new);
	}
	
	
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(NvVillagerModel.BODY_LAYER, NvVillagerModel::createBodyLayer);
		event.registerLayerDefinition(NvVillagerModel.CLOTHES_LAYER, NvVillagerModel::createClothesLayer);
		event.registerLayerDefinition(NvVillagerModel.HAIR_LAYER, NvVillagerModel::createHairLayer);
	}
	
	@SubscribeEvent
	public static void onRegisterClientReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath(Villagium.MODID, "mod_reload_listener"), RELOAD_LISTENER);
		event.addListener(Identifier.fromNamespaceAndPath(Villagium.MODID, "tones_loader"), TONES_LOADER);
		event.addListener(Identifier.fromNamespaceAndPath(Villagium.MODID, "appearance_loader"), APPEARANCE_LOADER);
	}
	
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		var level = Minecraft.getInstance().level;
		if (level == null)
			return;
		
		// Iteramos sobre las entidades cargadas en el cliente
		level.entitiesForRendering().forEach(entity -> {
			if (entity instanceof LivingEntity living)
				TempAnimManager.tick(living);
		});
		
		if (activeInteractionMob != null) {
			if (Minecraft.getInstance().gui.screen() == null
			    || !activeInteractionMob.isAlive()
			    || activeInteractionMob.level() != level) {
				activeInteractionMob = null;
			} else {
				var player = Minecraft.getInstance().player;
				if (player != null) {
					if (player.distanceToSqr(activeInteractionMob) > 49.0) {
						Minecraft.getInstance().setScreenAndShow(null);
						player.closeContainer();
						activeInteractionMob = null;
					} else {
						net.minecraft.world.phys.Vec3 playerEyePos = player.getEyePosition();
						net.minecraft.world.phys.Vec3 npcEyePos = activeInteractionMob.getEyePosition();
						
						double dx = npcEyePos.x - playerEyePos.x;
						double dy = npcEyePos.y - playerEyePos.y;
						double dz = npcEyePos.z - playerEyePos.z;
						
						double dh = Math.sqrt(dx * dx + dz * dz);
						
						float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
						float targetPitch = (float) (-(Math.atan2(dy, dh) * 180.0D / Math.PI));
						
						float currentYaw = player.getYRot();
						float currentPitch = player.getXRot();
						
						float yawDiff = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
						float pitchDiff = targetPitch - currentPitch;
						
						float speed = 0.25f;
						float newYaw = currentYaw + yawDiff * speed;
						float newPitch = currentPitch + pitchDiff * speed;
						
						player.setYRot(newYaw);
						player.setXRot(newPitch);
						player.yHeadRot = newYaw;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderFrame(RenderFrameEvent.Pre event) {
		var level = Minecraft.getInstance().level;
		if (level == null || activeInteractionMob == null)
			return;
		
		var player = Minecraft.getInstance().player;
		if (player != null) {
			float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
			net.minecraft.world.phys.Vec3 playerEyePos = player.getEyePosition(partialTicks);
			net.minecraft.world.phys.Vec3 npcEyePos = activeInteractionMob.getEyePosition(partialTicks);
			
			double dx = npcEyePos.x - playerEyePos.x;
			double dy = npcEyePos.y - playerEyePos.y;
			double dz = npcEyePos.z - playerEyePos.z;
			
			double dh = Math.sqrt(dx * dx + dz * dz);
			
			float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
			float targetPitch = (float) (-(Math.atan2(dy, dh) * 180.0D / Math.PI));
			
			float currentYaw = player.getYRot();
			float currentPitch = player.getXRot();
			
			float yawDiff = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
			float pitchDiff = targetPitch - currentPitch;
			
			float speed = 0.15f;
			float newYaw = currentYaw + yawDiff * speed;
			float newPitch = currentPitch + pitchDiff * speed;
			
			player.setYRot(newYaw);
			player.setXRot(newPitch);
			player.yHeadRot = newYaw;
		}
	}
	
	@SubscribeEvent
	public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
		LOGGER.info("Client connected. Syncing animations list to server...");
		NetworkManager.PIPELINE.sendToServer(new SyncRegisteredAnimationsToServerPacket(SharedAnimationData.getEntries()));
	}
}