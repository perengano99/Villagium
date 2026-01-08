package com.perengano99.villagium.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

public class Box {
	
	public final TexturedQuad[] quads;
	public final float posX1;
	public final float posY1;
	public final float posZ1;
	public final float posX2;
	public final float posY2;
	public final float posZ2;
	
	public Box(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
		this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 5);
	}
	
	protected Box(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, int quads) {
		this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, delta, delta, mirror, quads, false);
	}
	
	public Box(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float xDlt, float yDlt, float zDlt, boolean mirror) {
		this(tW, tH, texU, texV, x, y, z, dx, dy, dz, xDlt, yDlt, zDlt, mirror, 5);
	}
	
	protected Box(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float xDlt, float yDlt, float zDlt, boolean mirror, int quads) {
		this(tW, tH, texU, texV, x, y, z, dx, dy, dz, xDlt, yDlt, zDlt, mirror, quads, false);
	}
	
	protected Box(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float xDlt, float yDlt, float zDlt, boolean mirror, int quads,
	              boolean extra) {
		this.posX1 = x;
		this.posY1 = y;
		this.posZ1 = z;
		this.posX2 = x + (float) dx;
		this.posY2 = y + (float) dy;
		this.posZ2 = z + (float) dz;
		this.quads = new TexturedQuad[quads];
		float f = x + (float) dx;
		float f1 = y + (float) dy;
		float f2 = z + (float) dz;
		x  = x - xDlt;
		y  = y - yDlt;
		z  = z - zDlt;
		f  = f + xDlt;
		f1 = f1 + yDlt;
		f2 = f2 + zDlt;
		if (mirror) {
			float f3 = f;
			f = x;
			x = f3;
		}
		initQuads(tW, tH, texU, texV, dx, dy, dz, mirror, extra, new PosTexVertex(f, y, z, 0.0F, 8.0F), new PosTexVertex(f, f1, z, 8.0F, 8.0F), new PosTexVertex(x, f1, z, 8.0F,
		                                                                                                                                                         0.0F),
		          new PosTexVertex(x, y, f2, 0.0F, 0.0F), new PosTexVertex(f, y, f2, 0.0F, 8.0F), new PosTexVertex(f, f1, f2, 8.0F, 8.0F), new PosTexVertex(x, f1, f2, 8.0F, 0.0F)
				, new PosTexVertex(x, y, z, 0.0F, 0.0F));
	}
	
	protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PosTexVertex vertex, PosTexVertex vertex1,
	                         PosTexVertex vertex2, PosTexVertex vertex3, PosTexVertex vertex4, PosTexVertex vertex5, PosTexVertex vertex6, PosTexVertex vertex7) {
		this.quads[0] = new TexturedQuad(texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tW, tH, mirror, Direction.EAST, vertex4, vertex, vertex1, vertex5);
		this.quads[1] = new TexturedQuad(texU, texV + dz, texU + dz, texV + dz + dy, tW, tH, mirror, Direction.WEST, vertex7, vertex3, vertex6, vertex2);
		this.quads[2] = new TexturedQuad(texU + dz, texV, texU + dz + dx, texV + dz, tW, tH, mirror, Direction.DOWN, vertex4, vertex3, vertex7, vertex);
		this.quads[3] = new TexturedQuad(texU + dz, texV + dz + 4, texU + dz + dx, texV + 1 + dz + dy, tW, tH - 1, mirror, Direction.UP, vertex1, vertex2, vertex6, vertex5);
		this.quads[4] = new TexturedQuad(texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tW, tH, mirror, Direction.NORTH, vertex, vertex7, vertex2, vertex1);
	}
	
	public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();
		
		for (var quad : quads) {
			Vector3f vector3f = new Vector3f(quad.normal.x, quad.normal.y, quad.normal.z).mul(matrix3f);
			float normalX = vector3f.x;
			float normalY = vector3f.y;
			float normalZ = vector3f.z;
			
			for (var vertex : quad.vertexPositions) {
				float j = vertex.x() / 16.0F;
				float k = vertex.y() / 16.0F;
				float l = vertex.z() / 16.0F;
				Vector4f vector4f = new Vector4f(j, k, l, 1.0F).mul(matrix4f);
				consumer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), color, vertex.u(), vertex.v(), packedOverlay, packedLight, normalX, normalY, normalZ);
			}
		}
	}
	
	public record PosTexVertex(float x, float y, float z, float u, float v) {
		
		@Contract("_, _ -> new")
		public @NonNull PosTexVertex withTexturePosition(float texU, float texV) {
			return new PosTexVertex(x, y, z, texU, texV);
		}
	}
	
	public static class TexturedQuad {
		
		public final PosTexVertex[] vertexPositions;
		public final Vector3f normal;
		
		public TexturedQuad(float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn,
		                    PosTexVertex @NonNull ... positionsIn) {
			if (positionsIn.length != 4) {
				throw new IllegalArgumentException("Wrong number of vertex's. Expected: 4, Received: " + positionsIn.length);
			}
			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].withTexturePosition(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].withTexturePosition(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].withTexturePosition(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].withTexturePosition(u2 / texWidth - f, v2 / texHeight - f1);
			if (mirrorIn) {
				int i = positionsIn.length;
				
				for (int j = 0; j < i / 2; ++j) {
					PosTexVertex vertex = positionsIn[j];
					positionsIn[j]         = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = vertex;
				}
			}
			
			var vec3i = directionIn.getUnitVec3i();
			this.normal = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
			if (mirrorIn)
				this.normal.mul(-1.0f, 1.0f, 1.0f);
		}
	}
}
