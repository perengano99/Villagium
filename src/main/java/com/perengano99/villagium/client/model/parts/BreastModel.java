package com.perengano99.villagium.client.model.parts;

import com.perengano99.villagium.client.model.Box;
import net.minecraft.core.Direction;

public class BreastModel extends Box {
	
	private BreastModel(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
		super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta + 0.01f, delta, delta, mirror);
	}
	
	public static BreastModel createSized(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, float delta, boolean mirror, float breastSize,
	                                      float chestOffsetZ) {
		float reducer = -1;
		if (breastSize < 0.84f) reducer++;
		if (breastSize < 0.72f) reducer++;
		int depth = (int) (4 - chestOffsetZ - reducer);
		return new BreastModel(tW, tH, texU, texV, x, y, z, dx, dy, depth, delta, mirror);
	}
	
	@Override
	protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PosTexVertex vertex, PosTexVertex vertex1,
	                         PosTexVertex vertex2, PosTexVertex vertex3, PosTexVertex vertex4, PosTexVertex vertex5, PosTexVertex vertex6, PosTexVertex vertex7) {
		
		this.quads[0] = new TexturedQuad(texU + 4 + dx, texV + 4, texU + 4 + dx + 4, texV + 4 + dy, tW, tH, mirror, Direction.EAST, vertex4, vertex, vertex1, vertex5);
		this.quads[1] = new TexturedQuad(texU, texV + 4, texU + 4, texV + 4 + dy, tW, tH, mirror, Direction.WEST, vertex7, vertex3, vertex6, vertex2);
		//this.quads[2] = new TexturedQuad(texU + 4, texV, texU + 4 + dx, texV + 4, tW, tH, mirror, Direction.DOWN, vertex4, vertex3, vertex7, vertex);
		
		//		Estas caras no se ven ya que estan dentro del cuerpo.
		this.quads[3] = new TexturedQuad(texU + 4, texV + 4 + 4, texU + 4 + dx, texV + 1 + 4 + dy, tW, tH - 1, mirror, Direction.UP, vertex1, vertex2, vertex6, vertex5);
		this.quads[4] = new TexturedQuad(texU + 4, texV + 4, texU + 4 + dx, texV + 4 + dy, tW, tH, mirror, Direction.NORTH, vertex, vertex7, vertex2, vertex1);
	}
}
