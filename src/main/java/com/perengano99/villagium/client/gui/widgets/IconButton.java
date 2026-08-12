package com.perengano99.villagium.client.gui.widgets;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class IconButton extends Button {
	
	public enum IconPosition { LEFT, RIGHT }
	
	private static final int ICON_PADDING = 4;
	private static final int TEXT_PADDING = 2;
	
	private final Component text;
	private final @Nullable Item iconItem;
	private final @Nullable Identifier iconTexture;
	private final IconPosition iconPosition;
	private final int iconWidth;
	private final int iconHeight;
	
	private final @Nullable Identifier bgTexture;
	private final @Nullable Identifier bgHoverTexture;
	
	private IconButton(Builder builder) {
		super(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, DEFAULT_NARRATION);
		setTooltip(builder.tooltip);
		this.text           = builder.message;
		this.iconItem       = builder.iconItem;
		this.iconTexture    = builder.iconTexture;
		this.iconPosition   = builder.iconPosition;
		this.iconWidth      = builder.iconWidth;
		this.iconHeight     = builder.iconHeight;
		this.bgTexture      = builder.bgTexture;
		this.bgHoverTexture = builder.bgHoverTexture;
	}
	
	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mx, int my, float partialTicks) {
		// Render background
		if (bgTexture != null) {
			Identifier texture = bgTexture;
			if (isHoveredOrFocused() && bgHoverTexture != null)
				texture = bgHoverTexture;
			
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), width, height);
		} else {
			// Render default background
			super.extractDefaultSprite(graphics);
		}
		
		boolean hasText = !text.equals(Component.empty());
		boolean hasIcon = iconItem != null || iconTexture != null;
		if (!hasText && !hasIcon) return;
		
		// Render only icon, forced to center
		if (hasIcon && !hasText) {
			int iconX = getX() + (width - iconWidth) / 2;
			int iconY = getY() + (height - iconHeight) / 2;
			
			renderIcon(graphics, iconX, iconY);
			return;
		}
		
		// Render only text
		if (!hasIcon) {
			extractMessage(graphics, TEXT_PADDING, TEXT_PADDING);
			return;
		}
		
		int iconY = getY() + (height - iconHeight) / 2;
		int iconX, textLeftPadding, textRightPadding;
		if (iconPosition == IconPosition.LEFT) {
			textLeftPadding  = iconWidth + (ICON_PADDING * 2);
			textRightPadding = TEXT_PADDING;
			iconX            = getX() + ICON_PADDING;
		} else {
			textLeftPadding  = TEXT_PADDING;
			textRightPadding = iconWidth + (ICON_PADDING * 2);
			iconX            = getX() + (width - iconWidth) - ICON_PADDING;
		}
		
		renderIcon(graphics, iconX, iconY);
		extractMessage(graphics, textLeftPadding, textRightPadding);
	}
	
	
	private void extractMessage(GuiGraphicsExtractor graphics, int leftPadding, int rightPadding) {
		ActiveTextCollector output = graphics.textRendererForWidget(this, HoveredTextEffects.NONE);
		
		int left = this.getX() + leftPadding;
		int right = this.getX() + this.getWidth() - rightPadding;
		int top = this.getY();
		int bottom = this.getY() + this.getHeight();
		output.acceptScrollingWithDefaultCenter(this.text, left, right, top, bottom);
	}
	
	private void renderIcon(GuiGraphicsExtractor graphics, int x, int y) {
		if (iconTexture != null)
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, iconTexture, x, y, iconWidth, iconHeight);
		else if (iconItem != null) {
			int iconX = x + (iconWidth - 16) / 2;
			int iconY = y + (iconHeight - 16) / 2;
			graphics.fakeItem(iconItem.getDefaultInstance(), iconX, iconY);
		}
	}
	
	@Contract("_ -> new")
	public static @NonNull Builder builder(OnPress onPress) {
		return new Builder(onPress);
	}
	
	public static class Builder {
		
		private final OnPress onPress;
		private int x = 0, y = 0, width = 150, height = 20;
		private @Nullable Tooltip tooltip = null;
		private Component message = Component.empty();
		
		private IconPosition iconPosition = IconPosition.LEFT;
		private @Nullable Item iconItem = null;
		private @Nullable Identifier iconTexture = null;
		private int iconWidth = 16, iconHeight = 16;
		
		private @Nullable Identifier bgTexture = null;
		private @Nullable Identifier bgHoverTexture = null;
		
		public Builder(OnPress onPress) {
			this.onPress = onPress;
		}
		
		public Builder message(Component message) {
			this.message = message;
			return this;
		}
		
		public Builder Tooltip(Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}
		
		public Builder size(int width, int height) {
			this.width  = width;
			this.height = height;
			return this;
		}
		
		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}
		
		public Builder icon(@Nullable Identifier iconTexture) {
			this.iconTexture = iconTexture;
			this.iconItem    = null;
			return this;
		}
		
		public Builder icon(@Nullable Item iconItem) {
			this.iconItem    = iconItem;
			this.iconTexture = null;
			return this;
		}
		
		public Builder iconPosition(IconPosition position) {
			this.iconPosition = position;
			return this;
		}
		
		public Builder iconSize(int width, int height) {
			this.iconWidth  = width;
			this.iconHeight = height;
			return this;
		}
		
		public Builder bgTexture(@Nullable Identifier bgTexture) {
			this.bgTexture = bgTexture;
			return this;
		}
		
		public Builder bgHoverTexture(@Nullable Identifier bgHoverTexture) {
			this.bgHoverTexture = bgHoverTexture;
			return this;
		}
		
		public IconButton build() {
			return new IconButton(this);
		}
	}
}
