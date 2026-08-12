package com.perengano99.villagium.client.gui.screens;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.gui.widgets.IconButton;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NpcInteractionScreen extends Screen {
	
	private static final Identifier INTERACT_BUTTON = Identifier.fromNamespaceAndPath(Villagium.MODID, "interaction/icon_interact_button");
	private static final Identifier INTERACT_BUTTON_HOVER = Identifier.fromNamespaceAndPath(Villagium.MODID, "interaction/icon_interact_button_hover");
	
	protected interface CanRenderButton {
		
		boolean canRenderButton(Player player);
	}
	
	private final @Nullable NpcInteractionScreen lastScreen;
	public final List<IconButton> interactButtons = new ArrayList<>();
	
	protected NpcInteractionScreen(@Nullable NpcInteractionScreen lastScreen) {
		super(Component.empty());
		this.lastScreen = lastScreen;
	}
	
	public @Nullable NpcInteractionScreen getLastScreen() {
		return lastScreen;
	}
	
	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		extractBackgroundBands(graphics);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public boolean isInGameUi() {
		return true;
	}
	
	protected void extractBackgroundBands(@NonNull GuiGraphicsExtractor graphics) {
		graphics.fillGradient(0, 0, this.width, 40, 0x99000000, 0);
		graphics.fillGradient(0, this.height - 35, this.width, this.height, 0, -1072689136);
	}
	
	
	protected @Nullable <T extends GuiEventListener & Renderable & NarratableEntry> T addInteractButton(Identifier icon, Component text, Button.OnPress onPress) {
		return addInteractButton(icon, text, onPress, null);
	}
	
	protected @Nullable <T extends GuiEventListener & Renderable & NarratableEntry> T addInteractButton(Identifier icon, Component text, Button.OnPress onPress,
	                                                                                                    InteractionScreen.CanRenderButton canRenderButton) {
		return addInteractButton(null, icon, text, onPress, canRenderButton);
	}
	
	protected @Nullable <T extends GuiEventListener & Renderable & NarratableEntry> T addInteractButton(Item icon, Component text, Button.OnPress onPress) {
		return addInteractButton(icon, text, onPress, null);
	}
	
	protected @Nullable <T extends GuiEventListener & Renderable & NarratableEntry> T addInteractButton(Item icon, Component text, Button.OnPress onPress,
	                                                                                                    InteractionScreen.CanRenderButton canRenderButton) {
		return addInteractButton(icon, null, text, onPress, canRenderButton);
	}
	
	private @Nullable <T extends GuiEventListener & Renderable & NarratableEntry> T addInteractButton(@Nullable Item icon, @Nullable Identifier iconTex, Component text,
	                                                                                                  Button.OnPress onPress,
	                                                                                                  @Nullable InteractionScreen.CanRenderButton canRenderButton) {
		if (canRenderButton != null && !canRenderButton.canRenderButton(this.minecraft.player)) return null;
		
		IconButton.Builder btnBuilder = IconButton.builder(onPress).message(text).size(118, 24).bgTexture(INTERACT_BUTTON).bgHoverTexture(INTERACT_BUTTON_HOVER);
		btnBuilder = icon != null ? btnBuilder.icon(icon) : iconTex != null ? btnBuilder.icon(iconTex) : btnBuilder;
		
		var btn = btnBuilder.build();
		interactButtons.add(btn);
		addRenderableWidget(btn);
		return (T) btn;
	}
	
	@Override
	protected void removeWidget(@NonNull GuiEventListener widget) {
		if (widget instanceof IconButton) interactButtons.remove(widget);
		super.removeWidget(widget);
	}
	
	@Override
	protected void clearWidgets() {
		interactButtons.clear();
		super.clearWidgets();
	}
	
	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
