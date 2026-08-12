package com.perengano99.villagium.client.gui.screens;


import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.VillagiumClient;
import com.perengano99.villagium.client.gui.util.ScreenAnimator;
import com.perengano99.villagium.client.gui.util.ScreenAnimator.Easing;
import com.perengano99.villagium.client.gui.widgets.IconButton;
import com.perengano99.villagium.client.gui.widgets.NpcCardWidget;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.C2S_CloseInteractionPacket;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.social.profile.ProfileData;
import com.perengano99.villagium.social.relationship.RelationTag;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import com.perengano99.villagium.entity.VillagiumMob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class InteractionScreen extends Screen {
	
	protected final ProfileData profile;
	private final List<NpcCardWidget.CardTag> cardTags = new ArrayList<>();
	private NpcCardWidget npcCardWidget;
	private long openTime = -1L;
	private float initialAngleDiff = 1.0f;
	private long cardCompleteTime = -1L;
	private boolean isExiting = false;
	private long exitStartTime = -1L;
	private Consumer<InteractionScreen> exitCallback = null;
	protected boolean transitionToOtherScreen = false;
	
	protected InteractionScreen(S2C_OpenInteractMenuPacket payload) {
		super(Component.empty());
		
		ClientLevel level = Minecraft.getInstance().level;
		VillagiumMob<?> mob = null;
		if (level != null && level.getEntity(payload.entityId()) instanceof VillagiumMob<?> m)
			mob = m;
		this.profile = payload.profileData().withEntity(mob);
		cardTags.addAll(payload.activePlayerTags().stream().map(id -> {
			RelationTag tag = VillagiumData.RELATION_TAGS.get(id);
			if (tag != null)
				return new NpcCardWidget.CardTag(Component.translatable(tag.displayKey()), tag.displayColor());
			return null;
		}).filter(Objects::nonNull).toList());
	}
	
	public final List<IconButton> interactButtons = new ArrayList<>();
	
	protected interface CanRenderButton {
		
		boolean canRenderButton(Player player);
	}
	
	@Override
	protected void init() {
		super.init();
		if (this.openTime == -1L) {
			this.openTime = Util.getMillis();
			var player = this.minecraft.player;
			var npc = VillagiumClient.activeInteractionMob;
			if (player != null && npc != null) {
				net.minecraft.world.phys.Vec3 playerEyePos = player.getEyePosition();
				net.minecraft.world.phys.Vec3 npcEyePos = npc.getEyePosition();
				double dx = npcEyePos.x - playerEyePos.x;
				double dy = npcEyePos.y - playerEyePos.y;
				double dz = npcEyePos.z - playerEyePos.z;
				double dh = Math.sqrt(dx * dx + dz * dz);
				float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
				float targetPitch = (float) (-(Math.atan2(dy, dh) * 180.0D / Math.PI));
				float yawDiff = net.minecraft.util.Mth.wrapDegrees(targetYaw - player.getYRot());
				float pitchDiff = targetPitch - player.getXRot();
				this.initialAngleDiff = Math.max(1.0f, (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff));
			} else
				this.initialAngleDiff = 1.0f;
		}
		
		this.npcCardWidget = new NpcCardWidget(this.width - 140, 0, 140, 95, this.profile, this.cardTags);
		this.addRenderableWidget(this.npcCardWidget);
	}
	
	protected void addInteractButton(Identifier icon, Component text, Button.OnPress onPress) {
		addInteractButton(icon, text, onPress, null);
	}
	
	protected void addInteractButton(Identifier icon, Component text, Button.OnPress onPress, CanRenderButton canRenderButton) {
		addInteractButton(null, icon, text, onPress, canRenderButton);
	}
	
	protected void addInteractButton(Item icon, Component text, Button.OnPress onPress) {
		addInteractButton(icon, text, onPress, null);
	}
	
	protected void addInteractButton(Item icon, Component text, Button.OnPress onPress, CanRenderButton canRenderButton) {
		addInteractButton(icon, null, text, onPress, canRenderButton);
	}
	
	private void addInteractButton(@Nullable Item icon, @Nullable Identifier iconTex, Component text, Button.OnPress onPress, @Nullable CanRenderButton canRenderButton) {
		if (canRenderButton != null && !canRenderButton.canRenderButton(this.minecraft.player)) return;
		
		IconButton.Builder btnBuilder = IconButton.builder(onPress).message(text).size(118, 24).bgTexture(INTERACT_BUTTON).bgHoverTexture(INTERACT_BUTTON_HOVER);
		btnBuilder = icon != null ? btnBuilder.icon(icon) : iconTex != null ? btnBuilder.icon(iconTex) : btnBuilder;
		btnBuilder.position(this.width - 124, 0);
		
		var btn = btnBuilder.build();
		interactButtons.add(btn);
		addRenderableWidget(btn);
	}
	
	private static final Identifier CONTAINER = Identifier.fromNamespaceAndPath(Villagium.MODID, "container/interaction_menu");
	private static final Identifier INTERACT_BUTTON = Identifier.fromNamespaceAndPath(Villagium.MODID, "interaction/icon_interact_button");
	private static final Identifier INTERACT_BUTTON_HOVER = Identifier.fromNamespaceAndPath(Villagium.MODID, "interaction/icon_interact_button_hover");
	
	public void animateOut(Consumer<InteractionScreen> callback) {
		this.exitCallback = callback;
		if (this.isExiting)
			return;
		this.isExiting = true;
		this.exitStartTime = Util.getMillis();
	}
	
	@Override
	public void onClose() {
		this.animateOut(screen -> super.onClose());
	}
	
	private float getCameraAngleDiff() {
		var player = this.minecraft.player;
		var npc = VillagiumClient.activeInteractionMob;
		if (player != null && npc != null) {
			net.minecraft.world.phys.Vec3 playerEyePos = player.getEyePosition();
			net.minecraft.world.phys.Vec3 npcEyePos = npc.getEyePosition();
			double dx = npcEyePos.x - playerEyePos.x;
			double dy = npcEyePos.y - playerEyePos.y;
			double dz = npcEyePos.z - playerEyePos.z;
			double dh = Math.sqrt(dx * dx + dz * dz);
			float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
			float targetPitch = (float) (-(Math.atan2(dy, dh) * 180.0D / Math.PI));
			float yawDiff = net.minecraft.util.Mth.wrapDegrees(targetYaw - player.getYRot());
			float pitchDiff = targetPitch - player.getXRot();
			return (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
		}
		return 0.0f;
	}
	
	private void recalculateLayout() {
		int cardHeight = this.npcCardWidget != null ? this.npcCardWidget.getHeight() : 95;
		int numButtons = this.interactButtons.size();
		int buttonPanelHeight = 12 + numButtons * 24;
		int totalHeight = cardHeight + 5 + buttonPanelHeight;
		int startY = (this.height - totalHeight) / 2;
		
		int cardTargetX = this.width - 140;
		int cardStartX = this.width;
		int cardX;
		
		float deployProgress;
		float tBtn = 0.0f;
		
		if (this.isExiting) {
			float tBtnExit = ScreenAnimator.getProgress(this.exitStartTime, 50L);
			deployProgress = 1.0f - Easing.EASE_IN_CUBIC.ease(tBtnExit);
			tBtn = 1.0f - tBtnExit;
			
			float tCardExit = ScreenAnimator.getProgress(this.exitStartTime, 150L, 50L);
			cardX = ScreenAnimator.animate(cardTargetX, cardStartX, tCardExit, Easing.EASE_IN_CUBIC);
			
			long elapsedExit = Util.getMillis() - this.exitStartTime;
			if (elapsedExit >= 200L && this.exitCallback != null) {
				var callback = this.exitCallback;
				this.exitCallback = null;
				callback.accept(this);
			}
		} else {
			long elapsed = this.openTime != -1L ? Util.getMillis() - this.openTime : 0L;
			float currentAngleDiff = getCameraAngleDiff();
			float tTime = ScreenAnimator.getProgress(this.openTime, 150L);
			float tTimeout = ScreenAnimator.getProgress(this.openTime, 200L);
			float tCamera = currentAngleDiff < 2.0F ? 1.0f : Math.clamp(1.0f - (currentAngleDiff / this.initialAngleDiff), 0.0f, 1.0f);
			float tCard = Math.clamp(tCamera, tTimeout, tTime);
			
			cardX = ScreenAnimator.animate(cardStartX, cardTargetX, tCard, Easing.EASE_OUT_BACK);
			
			if ((tCard >= 1.0f || elapsed >= 200L) && this.cardCompleteTime == -1L)
				this.cardCompleteTime = Util.getMillis();
			
			tBtn = ScreenAnimator.getProgress(this.cardCompleteTime, 50L);
			deployProgress = Easing.EASE_OUT_CUBIC.ease(tBtn);
		}
		
		if (this.npcCardWidget != null) {
			this.npcCardWidget.setX(cardX);
			this.npcCardWidget.setY(startY);
		}
		
		int buttonPanelY = startY + cardHeight + 5;
		for (int i = 0; i < numButtons; i++) {
			IconButton btn = this.interactButtons.get(i);
			int btnTargetY = buttonPanelY + 6 + i * 24;
			int btnY = ScreenAnimator.animate(buttonPanelY, btnTargetY, deployProgress, Easing.LINEAR);
			btn.setX(this.width - 124);
			btn.setY(btnY);
			btn.visible = tBtn > 0.01f;
		}
	}
	
	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		float fadeProgress;
		if (this.isExiting) {
			float tExit = ScreenAnimator.getProgress(this.exitStartTime, 200L);
			fadeProgress = 1.0f - tExit;
		} else
			fadeProgress = ScreenAnimator.getProgress(this.openTime, 100L);
		
		graphics.fillGradient(0, 0, this.width, 40, ScreenAnimator.getFadeColor(0x99000000, fadeProgress), 0);
		graphics.fillGradient(0, this.height - 35, this.width, this.height, 0, ScreenAnimator.getFadeColor(-1072689136, fadeProgress));
		
		this.minecraft.gui.hud.extractDeferredSubtitles();
		recalculateLayout();
		
		int numButtons = this.interactButtons.size();
		int buttonPanelHeight = 12 + numButtons * 24;
		int cardHeight = this.npcCardWidget != null ? this.npcCardWidget.getHeight() : 95;
		int totalHeight = cardHeight + 5 + buttonPanelHeight;
		int startY = (this.height - totalHeight) / 2;
		int buttonPanelY = startY + cardHeight + 5;
		
		float deployProgress;
		if (this.isExiting) {
			float tExitBtn = ScreenAnimator.getProgress(this.exitStartTime, 50L);
			deployProgress = 1.0f - Easing.EASE_IN_CUBIC.ease(tExitBtn);
		} else {
			float tBtn = ScreenAnimator.getProgress(this.cardCompleteTime, 50L);
			deployProgress = Easing.EASE_OUT_CUBIC.ease(tBtn);
		}
		
		int animatedPanelHeight = (int) (buttonPanelHeight * deployProgress);
		if (animatedPanelHeight > 0)
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CONTAINER, width - 130, buttonPanelY, 128, animatedPanelHeight);
	}
	
	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
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
	public final boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public final boolean isInGameUi() {
		return true;
	}
	
	@Override
	public void removed() {
		super.removed();
		Logger.getLogger().info("InteractionScreen removed, transitionToOtherScreen: " + this.transitionToOtherScreen);
		if (!this.transitionToOtherScreen) {
			if (this.profile != null && this.profile.entity() != null)
				NetworkManager.PIPELINE.sendToServer(new C2S_CloseInteractionPacket(this.profile.entity().getId()));
			
			if (VillagiumClient.activeInteractionMob == (this.profile != null ? this.profile.entity() : null))
				VillagiumClient.activeInteractionMob = null;
		}
	}
}
