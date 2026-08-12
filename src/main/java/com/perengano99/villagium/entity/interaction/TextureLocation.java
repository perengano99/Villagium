package com.perengano99.villagium.entity.interaction;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * References a texture layout region from singular files or shared atlases.
 * Handles coordinates, dimensions, scaling, and blit drawing execution.
 */
public record TextureLocation(
		Identifier textureId,
		int u,
		int v,
		int width,
		int height,
		int textureWidth,
		int textureHeight
) {
	/**
	 * Creates a singular full-size texture.
	 */
	public static TextureLocation singular(Identifier id, int w, int h) {
		return new TextureLocation(id, 0, 0, w, h, w, h);
	}

	/**
	 * Draws the texture at screen coordinates.
	 */
	public void draw(GuiGraphicsExtractor gg, int x, int y) {
		gg.blit(textureId, x, y, width, height, (float) u, (float) v, (float) width, (float) height);
	}

	/**
	 * Draws the texture scaled/stretched to specific dimensions.
	 */
	public void draw(GuiGraphicsExtractor gg, int x, int y, int drawW, int drawH) {
		gg.blit(textureId, x, y, drawW, drawH, (float) u, (float) v, (float) width, (float) height);
	}
}
