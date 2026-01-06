package com.perengano99.villagium.client;

import com.perengano99.villagium.Villagium;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Villagium.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Villagium.MODID, value = Dist.CLIENT)
public final class VillagiumClient {
	
	public VillagiumClient(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		Villagium.LOGGER.info("HELLO FROM CLIENT SETUP");
		Villagium.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
	}
}