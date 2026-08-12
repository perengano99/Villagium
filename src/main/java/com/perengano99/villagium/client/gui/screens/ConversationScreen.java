package com.perengano99.villagium.client.gui.screens;

import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.C2S_CancelConversationPacket;
import com.perengano99.villagium.network.packets.C2S_SelectConversationChoicePacket;
import com.perengano99.villagium.network.packets.S2C_UpdateConversationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class ConversationScreen extends Screen {
	
	private S2C_UpdateConversationPacket payload;
	private Component dialogueText;
	private List<S2C_UpdateConversationPacket.ChoiceClientData> choices = new ArrayList<>();
	private Component npcName = Component.empty();
	private boolean sentCancellation = false;

	public ConversationScreen(S2C_UpdateConversationPacket payload) {
		super(Component.empty());
		this.payload = payload;
		this.dialogueText = payload.villagerText();
		this.choices = payload.choices();
		
		net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			net.minecraft.world.entity.Entity entity = level.getEntity(payload.entityId());
			if (entity != null)
				this.npcName = entity.getDisplayName();
		}
	}

	public void updateData(S2C_UpdateConversationPacket payload) {
		this.payload = payload;
		this.dialogueText = payload.villagerText();
		this.choices = payload.choices();
		this.sentCancellation = false;
		
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			Entity entity = level.getEntity(payload.entityId());
			if (entity != null)
				this.npcName = entity.getDisplayName();
		}
		
		this.clearWidgets();
		this.init();
	}

	@Override
	protected void init() {
		super.init();
		
		int buttonWidth = 220;
		int buttonHeight = 20;
		int startY = this.height / 2 + 10;
		
		for (int i = 0; i < choices.size(); i++) {
			var choice = choices.get(i);
			int finalI = i;
			Button btn = Button.builder(Component.translatable(choice.textKey()), button -> {
				this.sentCancellation = true;
				NetworkManager.PIPELINE.sendToServer(new C2S_SelectConversationChoicePacket(payload.entityId(), finalI));
			})
			.size(buttonWidth, buttonHeight)
			.pos(this.width / 2 - buttonWidth / 2, startY + i * 24)
			.build();
			btn.active = choice.available();
			this.addRenderableWidget(btn);
		}

		int cancelY = startY + choices.size() * 24;
		Button cancelBtn = Button.builder(Component.translatable("gui.villagium.interaction.back"), button -> {
			this.onClose();
		})
		.size(100, 20)
		.pos(this.width / 2 - 50, cancelY)
		.build();
		this.addRenderableWidget(cancelBtn);
	}

	@Override
	public void onClose() {
		if (!this.sentCancellation) {
			this.sentCancellation = true;
			NetworkManager.PIPELINE.sendToServer(new C2S_CancelConversationPacket(payload.entityId()));
		}
		super.onClose();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		
		int panelWidth = 320;
		int panelHeight = 80;
		int panelX = this.width / 2 - panelWidth / 2;
		int panelY = this.height / 2 - 90;
		
		graphics.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA000000, 0xAA000000);
		graphics.text(this.font, this.npcName, panelX + 10, panelY - 15, 0xFFFFAA00, false);

		List<FormattedCharSequence> lines = this.font.split(this.dialogueText, panelWidth - 20);
		for (int i = 0; i < lines.size(); i++)
			graphics.text(this.font, lines.get(i), panelX + 10, panelY + 10 + i * 12, 0xFFFFFFFF, false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
