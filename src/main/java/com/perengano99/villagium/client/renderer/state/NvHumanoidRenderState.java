package com.perengano99.villagium.client.renderer.state;

import com.perengano99.villagium.client.animations.face.FaceModelAnimator;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.entity.BreastSettings;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class NvHumanoidRenderState extends HumanoidRenderState {
	
	
	public FaceModelAnimator<?> faceModelController;
	public long gameTime;
	public RandomSource levelRandom;
	
	public Identifier faceTexture;
	public int irisColor;
	
	public boolean isFemale;
	public float breastCurrentSize;
	public float breastCurrentZOffset;
	
	public void buildBreast(VillagiumMob<?> entity, BreastSettings settings, BreastPhysicsState physicsState, float partialTicks) {
		// Si se llama a buildBreast, es vieja.
		
		isFemale             = true;
		breastCurrentSize    = settings.getSize();
		breastCurrentZOffset = settings.getOffsetZ();
		
		boolean breath = (!entity.isUnderWater() || entity.hasEffect(MobEffects.WATER_BREATHING) || entity.level().getBlockState(
				new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ())).is(Blocks.BUBBLE_COLUMN));
		
		NvHumanoidRenderer.breastsRenderer.
				build(physicsState, settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ(), settings.getSize(), settings.getOutward(), breath, partialTicks);
	}
}
