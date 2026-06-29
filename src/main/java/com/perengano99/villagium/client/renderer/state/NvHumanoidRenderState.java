package com.perengano99.villagium.client.renderer.state;

import com.perengano99.villagium.client.animation.ActiveAnimationRenderState;
import com.perengano99.villagium.client.animation.BakedAnimationHolder;
import com.perengano99.villagium.client.animation.face.FaceModelAnimator;
import com.perengano99.villagium.client.renderer.entity.NvHumanoidRenderer;
import com.perengano99.villagium.entity.BreastSettings;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class NvHumanoidRenderState extends HumanoidRenderState {
	
	public FaceModelAnimator<?> faceModelController;
	public long gameTime;
	public RandomSource levelRandom;
	
	public Identifier clothesTexture;
	public Identifier hairTexture;
	public Identifier faceTexture;
	public int irisColor;
	public int hairColor;
	
	public List<ActiveAnimationRenderState> activeAnimations = new ArrayList<>();
	
	public boolean isFemale;
	public float breastCurrentSize;
	public float breastCurrentZOffset;
	
	public int entityId;
	public boolean isPanicking;
	public boolean isRunning;
	public boolean useAltIdle;
	public String activeTriggerId;
	public BakedAnimationHolder bakedAnimationHolder;
	
	public NvHumanoidRenderState() {
	}
	
	public void buildBreast(VillagiumMob<?> entity, com.perengano99.villagium.entity.BreastSettings settings, BreastPhysicsState physicsState, float partialTicks) {
		isFemale = entity.getData(com.perengano99.villagium.core.registration.ModAttachments.PROFILE_DATA.get()).gender() == com.perengano99.villagium.social.profile.NvGender.FEMALE;
		if (isFemale) {
			breastCurrentSize    = settings.getSize();
			breastCurrentZOffset = settings.getOffsetZ();
			
			boolean breath = (!entity.isUnderWater() || entity.hasEffect(MobEffects.WATER_BREATHING) || entity.level().getBlockState(
					new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ())).is(Blocks.BUBBLE_COLUMN));
			
			NvHumanoidRenderer.breastsRenderer.
					build(physicsState, settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ(), settings.getSize(), settings.getOutward(), breath, partialTicks);
		} else {
			breastCurrentSize    = 0.0F;
			breastCurrentZOffset = 0.0F;
		}
	}
}
