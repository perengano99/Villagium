package com.perengano99.villagium.client;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.model.NvVillagerModel;
import com.perengano99.villagium.client.animations.BreastPhysicsManager;
import com.perengano99.villagium.client.renderer.entity.NvVillagerRenderer;
import com.perengano99.villagium.core.registration.ModEntityTypes;
import com.perengano99.villagium.core.util.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = Villagium.MODID, value = Dist.CLIENT)
public final class VillagiumClient {
	
	private static final Logger LOGGER = Logger.getLogger();
	
	private static final ResourcesReloadListener RELOAD_LISTENER = new ResourcesReloadListener();
	
	public VillagiumClient(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		LOGGER.info("HELLO FROM CLIENT SETUP");
		LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
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
	}
	
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		
		// Iteramos sobre las entidades cargadas en el cliente
		level.entitiesForRendering().forEach(entity -> {
			if (entity instanceof LivingEntity living) {
				// Aquí llamamos al manager para actualizar físicas
				BreastPhysicsManager.tick(living);
			}
		});
	}
}