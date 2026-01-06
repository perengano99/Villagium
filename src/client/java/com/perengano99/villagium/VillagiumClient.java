package com.perengano99.villagium;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class VillagiumClient {
	
	public static void onClientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			Villagium.LOGGER.info("HELLO FROM CLIENT SETUP");
			Villagium.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		});
	}
}


