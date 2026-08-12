package com.perengano99.villagium.client.gui.widgets;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NpcCardWidget extends AbstractWidget {
	
	private static final Identifier NPC_CARD_CONTAINER = Identifier.fromNamespaceAndPath(Villagium.MODID, "container/npc_card");
	private static final Identifier TAG_CONTAINER = Identifier.fromNamespaceAndPath(Villagium.MODID, "interaction/tag_container");
	
	private final ProfileData profile;
	private final List<CardTag> tags;
	private final List<TagLayout> tagLayouts = new ArrayList<>();
	private final NpcPortraitWidget portraitWidget;
	private int cachedHeight = 95;
	
	public NpcCardWidget(int x, int y, int width, int height, ProfileData profile, List<CardTag> tags) {
		super(x, y, width, height, Component.empty());
		this.profile = profile;
		this.tags    = tags;
		this.portraitWidget = new NpcPortraitWidget(x + 6, y + 6, 42, 42, profile != null ? profile.entity() : null);
		updateLayout(Minecraft.getInstance().font);
	}
	
	public void updateLayout(Font font) {
		tagLayouts.clear();
		int startX = 10;
		int startY = 60;
		int maxX = 120;
		int currentX = startX;
		int currentY = startY;
		int rowHeight = 15;
		int rowCount = 1;
		
		for (int i = 0; i < tags.size(); i++) {
			CardTag tag = tags.get(i);
			float scale = Math.max(0.45f, 1f - 0.15f * i);
			int textW = font.width(tag.text());
			int tagW = textW + 4;
			float scaledTagW = tagW * scale;
			
			if (currentX + scaledTagW > maxX && currentX > startX) {
				currentX = startX;
				currentY += rowHeight;
				rowCount++;
			}
			
			int bgW = textW + 6;
			tagLayouts.add(new TagLayout(tag, scale, currentX, currentY, bgW));
			currentX += (int) (scaledTagW + 4);
		}
		
		cachedHeight = 80 + 7 * rowCount;
		this.height  = cachedHeight;
	}
	
	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NPC_CARD_CONTAINER, getX(), getY(), getWidth(), getHeight());
		
		int textStartX = getX() + 10;
		if (profile != null && profile.entity() != null) {
			portraitWidget.setX(getX() + 6);
			portraitWidget.setY(getY() + 6);
			portraitWidget.setEntity(profile.entity());
			portraitWidget.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
			textStartX = getX() + 52;
		}
		
		Font font = Minecraft.getInstance().font;
		graphics.pose().pushMatrix();
		graphics.pose().scale(1.4f);
		graphics.text(font, profile != null ? profile.displayName() : Component.empty(), (int) (textStartX / 1.4f), (int) ((getY() + 8) / 1.4f), 0xFF020202, false);
		graphics.pose().popMatrix();
		
		if (profile != null) {
			var mood = profile.mood();
			if (mood.display())
				graphics.text(font, Component.translatable(mood.displayKey()), textStartX, getY() + 26, mood.getDisplayColorInt(), false);
		}
		
		for (TagLayout layout : tagLayouts) {
			graphics.pose().pushMatrix();
			graphics.pose().translate((float) (getX() + layout.x), (float) (getY() + layout.y));
			graphics.pose().scale(layout.scale);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TAG_CONTAINER, -3, -2, layout.bgW, 12, layout.tag.color());
			graphics.text(font, layout.tag.text(), 0, 0, 0xFF020202, false);
			graphics.pose().popMatrix();
		}
	}
	
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
	
	public record CardTag(Component text, int color) {}
	
	private record TagLayout(CardTag tag, float scale, int x, int y, int bgW) {}
}
