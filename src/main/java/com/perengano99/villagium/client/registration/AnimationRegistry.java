package com.perengano99.villagium.client.registration;

import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.client.animation.AnimationRule;
import com.perengano99.villagium.client.animation.ModelAnimation;
import com.perengano99.villagium.client.animation.TemporalVariantConfig;
import com.perengano99.villagium.client.animation.definitions.villager.*;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.entity.npc.NvVillager;
import com.perengano99.villagium.network.SharedAnimationData;

import java.util.*;

public class AnimationRegistry {
	
	public static final List<String> ANIMATION_IDS = new ArrayList<>();
	private static final Map<AnimationCategory, List<String>> CATEGORY_TO_IDS = new EnumMap<>(AnimationCategory.class);
	
	public record RegisteredAnimation(ModelAnimation animation, AnimationRule rule) {}
	
	public record TemporalVariant(ModelAnimation variant, TemporalVariantConfig config) {}
	
	private static final Map<AnimationCategory, List<RegisteredAnimation>> REGISTRY = new EnumMap<>(AnimationCategory.class);
	private static final Map<String, List<TemporalVariant>> TEMPORAL_VARIANTS = new HashMap<>();
	
	static {
		for (AnimationCategory category : AnimationCategory.values()) {
			REGISTRY.put(category, new ArrayList<>());
			CATEGORY_TO_IDS.put(category, new ArrayList<>());
		}
		registerDefaults();
	}
	
	private static void registerId(String id, AnimationCategory category) {
		List<String> list = CATEGORY_TO_IDS.get(category);
		if (!list.contains(id))
			list.add(id);
		if (!ANIMATION_IDS.contains(id))
			ANIMATION_IDS.add(id);
		SharedAnimationData.registerId(id, category);
	}
	
	public static List<String> getIdsForCategory(String categoryName) {
		try {
			AnimationCategory category = AnimationCategory.valueOf(categoryName.toUpperCase(Locale.ROOT));
			return CATEGORY_TO_IDS.getOrDefault(category, List.of());
		} catch (IllegalArgumentException e) {
			return List.of();
		}
	}
	
	public static Optional<ModelAnimation> getAnimationById(String id) {
		for (List<RegisteredAnimation> list : REGISTRY.values()) {
			for (RegisteredAnimation reg : list) {
				if (reg.animation().getId().equals(id))
					return Optional.of(reg.animation());
			}
		}
		for (List<TemporalVariant> list : TEMPORAL_VARIANTS.values()) {
			for (TemporalVariant variant : list) {
				if (variant.variant().getId().equals(id))
					return Optional.of(variant.variant());
			}
		}
		return Optional.empty();
	}
	
	public static void register(ModelAnimation animation, AnimationRule rule) {
		REGISTRY.get(animation.getCategory()).add(new RegisteredAnimation(animation, rule));
		registerId(animation.getId(), animation.getCategory());
	}
	
	public static void registerTemporalVariant(String baseAnimId, ModelAnimation variant, TemporalVariantConfig config) {
		TEMPORAL_VARIANTS.computeIfAbsent(baseAnimId, k -> new ArrayList<>()).add(new TemporalVariant(variant, config));
		registerId(variant.getId(), variant.getCategory());
	}
	
	public static List<RegisteredAnimation> getForCategory(AnimationCategory category) {
		return REGISTRY.get(category);
	}
	
	public static List<TemporalVariant> getTemporalVariants(String baseAnimId) {
		return TEMPORAL_VARIANTS.getOrDefault(baseAnimId, List.of());
	}
	
	private static void registerDefaults() {
		// 1. IDLE Animations
		register(new VillagerIdleMaleAnimation(), (entity, controller) -> controller.isFemale() ? 0.0f : 1.0f);
		register(new VillagerIdleFemaleAnimation(), (entity, controller) -> controller.isFemale() ? 1.0f : 0.0f);
		
		//		register(new IdleAnimation(), (entity, controller) -> {
		//			if (entity instanceof VillagiumMob<?> mob)
		//				return mob.walkAnimation.speed() <= 0.01f ? 1.0f : 0.0f;
		//			return 0.0f;
		//		});
		//
		//		registerTemporalVariant("idle", new IdleFemaleUpperAltAnimation(), new TemporalVariantConfig(160, 0.4f, 120, 200));
		//		registerTemporalVariant("idle", new IdleMaleUpperAltAnimation(), new TemporalVariantConfig(160, 0.4f, 120, 200));
		
		// 2. POSE Animations
		//		register(new FemaleConversationPoseAnimation(), (entity, controller) -> {
		//			if (entity instanceof VillagiumMob<?> mob && controller.isFemale()) {
		//				if (mob.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET))
		//					return 1.0f;
		//			}
		//			return 0.0f;
		//		});
		
		// 3. MOVEMENT Animations
		register(new VillagerMovementLegsAnimation(), (entity, controller) -> 0.0f);
		
		register(new VillagerWalkFemaleAnimation(), (entity, controller) -> {
			if (entity instanceof VillagiumMob<?> mob && controller.isFemale()) {
				boolean isMoving = mob.walkAnimation.speed() > 0.01f;
				if (isMoving && !mob.isPanicking() && !mob.isRunning() && controller.getWalkVariantIndex() == 0)
					return 1.0f;
			}
			return 0.0f;
		});
		
		register(new VillagerWalkFemaleCuteAnimation(), (entity, controller) -> {
			if (entity instanceof VillagiumMob<?> mob && controller.isFemale()) {
				boolean isMoving = mob.walkAnimation.speed() > 0.01f;
				if (isMoving && !mob.isPanicking() && !mob.isRunning() && controller.getWalkVariantIndex() == 1)
					return 1.0f;
			}
			return 0.0f;
		});
		
		register(new VillagerWalkMaleAnimation(), (entity, controller) -> {
			if (entity instanceof NvVillager mob && !controller.isFemale()) {
				boolean isMoving = mob.walkAnimation.speed() > 0.01f;
				if (isMoving && !mob.isPanicking() && !mob.isRunning() && controller.getWalkVariantIndex() == 0)
					return 1.0f;
			}
			return 0.0f;
		});
		
		register(new VillagerRunFemaleAnimation(), (entity, controller) -> {
			if (entity instanceof VillagiumMob<?> mob && controller.isFemale()) {
				boolean isMoving = mob.walkAnimation.speed() > 0.01f;
				if (isMoving && mob.isRunning() && !mob.isPanicking())
					return 5.0f;
			}
			return 0.0f;
		});
		
		register(new VillagerRunMaleAnimation(), (entity, controller) -> {
			if (entity instanceof VillagiumMob<?> mob && !controller.isFemale()) {
				boolean isMoving = mob.walkAnimation.speed() > 0.01f;
				if (isMoving && mob.isRunning() && !mob.isPanicking())
					return 5.0f;
			}
			return 0.0f;
		});
		
		// 4. ACTION Animations
		register(new GreetAnimation(), (entity, controller) -> {
			if (entity instanceof VillagiumMob<?> mob)
				return "greet".equals(mob.getActiveTriggerId()) ? 1.0f : 0.0f;
			return 0.0f;
		});
	}
}
