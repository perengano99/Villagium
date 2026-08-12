package com.perengano99.villagium.client.gui.screens;

import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.C2S_StartConversationPacket;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class VillagerInteractionScreen extends InteractionScreen {
	
	public VillagerInteractionScreen(S2C_OpenInteractMenuPacket payload) {
		super(payload);
	}
	
	@Override
	protected void init() {
		super.init();
		
		addInteractButton(Items.BAMBOO, Component.literal("Comerciar"), button -> {
			System.out.println("El botón está jalando wey");
		});
		addInteractButton(Items.CLOCK, Component.literal("Conversar"), button -> {
			if (this.profile != null && this.profile.entity() != null)
				this.animateOut(screen -> {
					this.transitionToOtherScreen = true;
					NetworkManager.PIPELINE.sendToServer(new C2S_StartConversationPacket(this.profile.entity().getId()));
				});
		});
		addInteractButton(Items.TORCHFLOWER, Component.literal("Seguir"), button -> {}, (p) -> true);
		addInteractButton(Items.CARVED_PUMPKIN, Component.literal("Regalar"), button -> {});
	}
}