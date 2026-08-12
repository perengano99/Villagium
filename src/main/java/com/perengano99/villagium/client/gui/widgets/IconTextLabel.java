package com.perengano99.villagium.client.gui.widgets;

import com.perengano99.villagium.entity.interaction.TextureLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class IconTextLabel extends AbstractWidget {

	public enum IconPosition {
		LEFT, RIGHT
	}

	private static final int ICON_SIZE_ITEM = 16;
	private static final int ICON_TEXT_PADDING = 4;
	private static final int HORIZONTAL_PADDING = 4;

	private final ItemStack iconStack;
	private final @Nullable TextureLocation iconTexture;
	private final IconPosition iconPosition;
	private final int iconWidth;
	private final int iconHeight;

	private final @Nullable TextureLocation customBgNormal;
	private final @Nullable TextureLocation customBgHover;
	private final int bgFrameCount;
	private final int bgFrameDurationMs;
	private final boolean bgHorizontal;

	private long hoverStartTime = -1;

	private IconTextLabel(Builder builder) {
		super(builder.x, builder.y, builder.width, builder.height, builder.message);
		this.setTooltip(builder.tooltip);
		this.iconStack = builder.iconStack;
		this.iconTexture = builder.iconTexture;
		this.iconPosition = builder.iconPosition;
		this.iconWidth = builder.iconWidth;
		this.iconHeight = builder.iconHeight;
		this.customBgNormal = builder.customBgNormal;
		this.customBgHover = builder.customBgHover;
		this.bgFrameCount = builder.bgFrameCount;
		this.bgFrameDurationMs = builder.bgFrameDurationMs;
		this.bgHorizontal = builder.bgHorizontal;
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor gg, int mouseX, int mouseY, float partialTicks) {
		TextureLocation customBg = customBgNormal;
		if (isHoveredOrFocused())
			customBg = customBgHover != null ? customBgHover : customBgNormal;

		if (customBg != null)
			drawCustomBackground(gg, customBg);

		boolean hasText = !this.getMessage().getString().isEmpty();
		boolean hasIcon = this.iconTexture != null || (this.iconStack != null && !this.iconStack.isEmpty());
		if (!hasIcon && !hasText)
			return;

		if (hasIcon && !hasText) {
			int iconX = this.getX() + (this.getWidth() - this.iconWidth) / 2;
			int iconY = this.getY() + (this.getHeight() - this.iconHeight) / 2;
			renderIcon(gg, iconX, iconY);
			return;
		}

		Font font = Minecraft.getInstance().font;
		int color = this.getFGColor();
		int textY = this.getY() + (this.getHeight() - 8) / 2;

		if (!hasIcon) {
			int textW = font.width(this.getMessage());
			int textMinX = this.getX() + HORIZONTAL_PADDING;
			int textMaxX = this.getX() + this.getWidth() - HORIZONTAL_PADDING;
			int availableTextWidth = textMaxX - textMinX;
			if (textW <= availableTextWidth) {
				int textX = this.getX() + (this.getWidth() - textW) / 2;
				gg.text(font, this.getMessage(), textX, textY, color, false);
			} else
				renderScrollingString(gg, font, textMinX, textMaxX, textY, textW, availableTextWidth, color);
			return;
		}

		int messageWidth = font.width(this.getMessage());
		int availableTextWidth = this.getWidth() - this.iconWidth - ICON_TEXT_PADDING - (HORIZONTAL_PADDING * 2);

		if (messageWidth <= availableTextWidth) {
			int contentWidth = this.iconWidth + ICON_TEXT_PADDING + messageWidth;
			int contentStartX = this.getX() + (this.getWidth() - contentWidth) / 2;
			int iconY = this.getY() + (this.getHeight() - this.iconHeight) / 2;

			int iconX;
			int textX;
			if (this.iconPosition == IconPosition.LEFT) {
				iconX = contentStartX;
				textX = iconX + this.iconWidth + ICON_TEXT_PADDING;
			} else {
				textX = contentStartX;
				iconX = textX + messageWidth + ICON_TEXT_PADDING;
			}

			renderIcon(gg, iconX, iconY);
			gg.text(font, this.getMessage(), textX, textY, color, false);
		} else {
			int iconY = this.getY() + (this.getHeight() - this.iconHeight) / 2;
			int textMinX;
			int textMaxX;

			if (this.iconPosition == IconPosition.LEFT) {
				int iconX = this.getX() + HORIZONTAL_PADDING;
				renderIcon(gg, iconX, iconY);
				textMinX = iconX + this.iconWidth + ICON_TEXT_PADDING;
				textMaxX = this.getX() + this.getWidth() - HORIZONTAL_PADDING;
			} else {
				int iconX = this.getX() + this.getWidth() - this.iconWidth - HORIZONTAL_PADDING;
				renderIcon(gg, iconX, iconY);
				textMinX = this.getX() + HORIZONTAL_PADDING;
				textMaxX = iconX - ICON_TEXT_PADDING;
			}

			renderScrollingString(gg, font, textMinX, textMaxX, textY, messageWidth, availableTextWidth, color);
		}
	}

	private void drawCustomBackground(GuiGraphicsExtractor gg, TextureLocation baseTex) {
		if (bgFrameCount > 1) {
			int frameIndex = (int) ((System.currentTimeMillis() / bgFrameDurationMs) % bgFrameCount);
			int uOffset = bgHorizontal ? frameIndex * baseTex.width() : 0;
			int vOffset = !bgHorizontal ? frameIndex * baseTex.height() : 0;
			gg.blit(
					baseTex.textureId(),
					getX(), getY(),
					width, height,
					(float) (baseTex.u() + uOffset),
					(float) (baseTex.v() + vOffset),
					(float) baseTex.width(),
					(float) baseTex.height()
			);
		} else
			baseTex.draw(gg, getX(), getY(), width, height);
	}

	private void renderScrollingString(GuiGraphicsExtractor gg, Font font, int textMinX, int textMaxX, int textY, int textW, int availableTextWidth, int color) {
		boolean hovered = isHoveredOrFocused();
		if (hovered) {
			if (hoverStartTime == -1)
				hoverStartTime = System.currentTimeMillis();
		} else
			hoverStartTime = -1;

		int scrollOffset = 0;
		if (hovered && hoverStartTime != -1) {
			long elapsedMs = System.currentTimeMillis() - hoverStartTime;
			if (elapsedMs > 1000) {
				int maxScroll = textW - availableTextWidth;
				int scrollRange = maxScroll + 20;
				int currentScroll = (int) (((elapsedMs - 1000) / 33) % scrollRange);
				if (currentScroll <= maxScroll)
					scrollOffset = currentScroll;
				else
					scrollOffset = maxScroll;
			}
		}

		gg.enableScissor(textMinX, getY(), textMaxX, getY() + height);
		gg.text(font, this.getMessage(), textMinX - scrollOffset, textY, color, false);
		gg.disableScissor();
	}

	private void renderIcon(GuiGraphicsExtractor gg, int x, int y) {
		if (this.iconTexture != null)
			this.iconTexture.draw(gg, x, y);
		else if (this.iconStack != null && !this.iconStack.isEmpty()) {
			int itemX = x + (this.iconWidth - ICON_SIZE_ITEM) / 2;
			int itemY = y + (this.iconHeight - ICON_SIZE_ITEM) / 2;
			gg.fakeItem(this.iconStack, itemX, itemY);
		}
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Component message = Component.empty();
		private int x = 0, y = 0, width = 150, height = 20;
		private @Nullable Tooltip tooltip = null;
		private ItemStack iconStack = ItemStack.EMPTY;
		private @Nullable TextureLocation iconTexture = null;
		private IconPosition iconPosition = IconPosition.LEFT;
		private int iconWidth = 0, iconHeight = 0;

		private @Nullable TextureLocation customBgNormal = null;
		private @Nullable TextureLocation customBgHover = null;
		private int bgFrameCount = 1;
		private int bgFrameDurationMs = 100;
		private boolean bgHorizontal = true;

		public Builder message(Component message) {
			this.message = message;
			return this;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder tooltip(Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public Builder icon(ItemStack iconStack) {
			this.iconStack = iconStack;
			this.iconWidth = ICON_SIZE_ITEM;
			this.iconHeight = ICON_SIZE_ITEM;
			return this;
		}

		public Builder icon(TextureLocation texture) {
			this.iconTexture = texture;
			this.iconWidth = texture.width();
			this.iconHeight = texture.height();
			return this;
		}

		public Builder iconPosition(IconPosition position) {
			this.iconPosition = position;
			return this;
		}

		public Builder customBg(TextureLocation normal) {
			this.customBgNormal = normal;
			return this;
		}

		public Builder customBg(TextureLocation normal, TextureLocation hover) {
			this.customBgNormal = normal;
			this.customBgHover = hover;
			return this;
		}

		public Builder animatedBg(TextureLocation sheet, int frameCount, int frameDurationMs, boolean horizontal) {
			this.customBgNormal = sheet;
			this.bgFrameCount = frameCount;
			this.bgFrameDurationMs = frameDurationMs;
			this.bgHorizontal = horizontal;
			return this;
		}

		public IconTextLabel build() {
			if (iconStack.isEmpty() && iconTexture == null && message.getString().isEmpty())
				throw new IllegalStateException("IconTextLabel must have an icon or a message.");
			return new IconTextLabel(this);
		}
	}
}
