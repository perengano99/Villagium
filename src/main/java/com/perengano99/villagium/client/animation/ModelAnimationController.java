package com.perengano99.villagium.client.animation;

import com.perengano99.villagium.client.registration.AnimationRegistry;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModelAnimationController {
	
	private static class ActiveTemporalState {
		
		final ModelAnimation variant;
		final TemporalVariantConfig config;
		int ticksRemaining;
		boolean isLooping;
		
		ActiveTemporalState(ModelAnimation variant, TemporalVariantConfig config, int duration, boolean isLooping) {
			this.variant        = variant;
			this.config         = config;
			this.ticksRemaining = duration;
			this.isLooping      = isLooping;
		}
	}
	
	private final LivingEntity entity;
	private final Map<AnimationCategory, ActiveAnimation> activeAnimations = new EnumMap<>(AnimationCategory.class);
	private final Map<AnimationCategory, ActiveTemporalState> activeTemporalStates = new EnumMap<>(AnimationCategory.class);
	private final Map<AnimationCategory, Integer> ticksRemaining = new EnumMap<>(AnimationCategory.class);
	private final Map<String, Integer> ticksSinceLastCheck = new HashMap<>();
	private final int walkVariantIndex;
	
	public ModelAnimationController(LivingEntity entity) {
		this.entity = entity;
		UUID uuid = entity.getUUID();
		this.walkVariantIndex = Math.abs(uuid.hashCode()) % 2;
	}
	
	public boolean isFemale() {
		return entity.getData(com.perengano99.villagium.core.registration.ModAttachments.PROFILE_DATA.get()).gender() == com.perengano99.villagium.social.profile.NvGender.FEMALE;
	}
	
	public Map<AnimationCategory, ActiveAnimation> getActiveAnimations() {
		return activeAnimations;
	}
	
	public int getWalkVariantIndex() {
		return walkVariantIndex;
	}
	
	// Command Playback Controller (Cancels active if exists, starts requested)
	public void play(ModelAnimation animation, @Nullable Boolean loopOverride, int durationTicks, float speedFactor, boolean isManual) {
		if (animation == null) return;
		AnimationCategory category = animation.getCategory();
		
		if (!isManual) {
			// Don't restart same animation if already playing in that category
			ActiveAnimation active = activeAnimations.get(category);
			if (active != null && active.getAnimation().getId().equals(animation.getId()) && active.getPhase() != ActiveAnimation.Phase.OUTRO)
				return;
			
			// Protect manual animations: if any manual animation is active, ignore automatic ones
			for (ActiveAnimation act : activeAnimations.values()) {
				if (act.isManual())
					return;
			}
			
			// Check priorities: ignore if another active category has higher or equal priority
			for (Map.Entry<AnimationCategory, ActiveAnimation> entry : activeAnimations.entrySet()) {
				AnimationCategory activeCat = entry.getKey();
				if (activeCat != category && activeCat.getPriority() >= category.getPriority())
					return;
			}
		}
		
		if (isManual) {
			// Stop all active animations in all categories immediately
			for (AnimationCategory cat : AnimationCategory.values()) {
				ActiveAnimation active = activeAnimations.remove(cat);
				if (active != null)
					active.getState().stop();
				ticksRemaining.remove(cat);
			}
		} else {
			// Stop and remove same category
			ActiveAnimation active = activeAnimations.remove(category);
			if (active != null)
				active.getState().stop();
			
			// Stop and remove lower priority categories
			for (AnimationCategory cat : AnimationCategory.values()) {
				if (cat != category && cat.getPriority() < category.getPriority()) {
					ActiveAnimation lowerActive = activeAnimations.remove(cat);
					if (lowerActive != null)
						lowerActive.getState().stop();
					ticksRemaining.remove(cat);
				}
			}
		}
		
		ActiveAnimation newActive = new ActiveAnimation(animation, loopOverride, speedFactor, isManual);
		newActive.getState().start(entity.tickCount);
		activeAnimations.put(category, newActive);
		
		if (durationTicks > 0)
			ticksRemaining.put(category, durationTicks);
		else
			ticksRemaining.remove(category);
	}
	
	public void play(ModelAnimation animation, @Nullable Boolean loopOverride, int durationTicks, boolean isManual) {
		play(animation, loopOverride, durationTicks, 1.0f, isManual);
	}
	
	public void stop(AnimationCategory category) {
		ActiveAnimation active = activeAnimations.get(category);
		if (active != null) {
			if (active.isManual()) {
				activeAnimations.remove(category);
				active.getState().stop(); // Stop manual animation immediately
			} else {
				active.stopOrOutro(entity.tickCount);
				if (active.isFinished())
					activeAnimations.remove(category);
			}
		}
		ticksRemaining.remove(category);
	}
	
	public void restart(AnimationCategory category) {
		ActiveAnimation active = activeAnimations.get(category);
		if (active != null)
			active.restart(entity.tickCount);
	}
	
	// Automatic playback used by tickMobStates
	public void play(ModelAnimation animation) {
		play(animation, null, 0, 1.0f, false);
	}
	
	public void tick() {
		List<AnimationCategory> expired = new java.util.ArrayList<>();
		for (Map.Entry<AnimationCategory, Integer> entry : ticksRemaining.entrySet()) {
			int remaining = entry.getValue() - 1;
			if (remaining <= 0)
				expired.add(entry.getKey());
			else
				entry.setValue(remaining);
		}
		for (AnimationCategory category : expired)
			stop(category);
		
		// Tick active animations, remove finished ones
		activeAnimations.entrySet().removeIf(entry -> {
			ActiveAnimation active = entry.getValue();
			ActiveAnimation.Phase oldPhase = active.getPhase();
			active.tick(entity.tickCount);
			
			if (active.isFinished())
				return true;
			
			if (active.getPhase() != oldPhase)
				active.getState().start(entity.tickCount);
			return false;
		});
		
		if (entity instanceof VillagiumMob<?> mob) {
			// If AI is frozen, skip automatic states ticking
			if (!mob.isNoAi())
				tickMobStates(mob);
		}
	}
	
	private void tickMobStates(VillagiumMob<?> mob) {
		for (AnimationCategory category : AnimationCategory.values()) {
			// Evaluate normal registry rules
			List<AnimationRegistry.RegisteredAnimation> registered = AnimationRegistry.getForCategory(category);
			AnimationRegistry.RegisteredAnimation best = null;
			float maxWeight = 0.0f;
			
			for (AnimationRegistry.RegisteredAnimation entry : registered) {
				float weight = entry.rule().getWeight(mob, this);
				if (weight > maxWeight) {
					maxWeight = weight;
					best      = entry;
				}
			}
			
			if (best != null) {
				ModelAnimation baseAnim = best.animation();
				String baseId = baseAnim.getId();
				ModelAnimation finalAnim = baseAnim;
				
				List<AnimationRegistry.TemporalVariant> variants = AnimationRegistry.getTemporalVariants(baseId);
				if (!variants.isEmpty()) {
					ActiveTemporalState tempState = activeTemporalStates.get(category);
					if (tempState != null) {
						if (tempState.isLooping) {
							tempState.ticksRemaining--;
							if (tempState.ticksRemaining <= 0)
								activeTemporalStates.remove(category);
							else
								finalAnim = tempState.variant;
						} else {
							ActiveAnimation active = activeAnimations.get(category);
							if (active == null || !active.getAnimation().getId().equals(tempState.variant.getId()))
								activeTemporalStates.remove(category);
							else
								finalAnim = tempState.variant;
						}
					} else {
						int elapsed = ticksSinceLastCheck.getOrDefault(baseId, 0) + 1;
						ticksSinceLastCheck.put(baseId, elapsed);
						
						for (AnimationRegistry.TemporalVariant variantEntry : variants) {
							String variantId = variantEntry.variant().getId();
							if (variantId.contains("female") && !isFemale())
								continue;
							if (variantId.contains("male") && isFemale())
								continue;
							
							TemporalVariantConfig config = variantEntry.config();
							if (elapsed >= config.checkIntervalTicks()) {
								ticksSinceLastCheck.put(baseId, 0);
								if (mob.level().getRandom().nextFloat() < config.activationChance()) {
									int min = config.minDurationTicks();
									int max = config.maxDurationTicks();
									int duration = min + mob.level().getRandom().nextInt(Math.max(1, max - min + 1));
									boolean isLooping = variantEntry.variant().isLoop();
									
									ActiveTemporalState newState = new ActiveTemporalState(
											variantEntry.variant(),
											config,
											duration,
											isLooping
									);
									activeTemporalStates.put(category, newState);
									finalAnim = newState.variant;
									break;
								}
							}
						}
					}
				} else
					activeTemporalStates.remove(category);
				
				play(finalAnim);
			} else {
				activeTemporalStates.remove(category);
				stop(category);
			}
		}
	}
}
