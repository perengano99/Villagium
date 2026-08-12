package com.perengano99.villagium.social.condition;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.profile.NvProfile;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public final class ConditionRegistry {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Map<String, ISocialCondition> CONDITIONS = Maps.newHashMap();

	public static void register(String id, ISocialCondition condition) {
		if (CONDITIONS.putIfAbsent(id, condition) != null)
			LOGGER.warn("Duplicate social condition registration for ID: {}", id);
	}

	@Nullable
	public static ISocialCondition get(String id) {
		return CONDITIONS.get(id);
	}

	static {
		register("mood", new ISocialCondition() {
			@Override
			public boolean check(SocialEventContext context, JsonElement params) {
				if (params == null || !params.isJsonPrimitive() || !params.getAsJsonPrimitive().isString())
					return false;
				Optional<VillagiumMob> villagerOpt = context.get(ContextKeys.VILLAGER);
				if (villagerOpt.isEmpty())
					return false;
				String activeMoodId = villagerOpt.get().getOrCreateProfile().getMood().id();
				return activeMoodId.equalsIgnoreCase(params.getAsString());
			}

			@Override
			public ExecutionSide getSide() {
				return ExecutionSide.SERVER;
			}
		});

		register("trait", new ISocialCondition() {
			@Override
			public boolean check(SocialEventContext context, JsonElement params) {
				if (params == null || !params.isJsonPrimitive() || !params.getAsJsonPrimitive().isString())
					return false;
				Optional<VillagiumMob> villagerOpt = context.get(ContextKeys.VILLAGER);
				if (villagerOpt.isEmpty())
					return false;
				NvProfile profile = villagerOpt.get().getOrCreateProfile();
				String paramStr = params.getAsString();
				Identifier paramId = Identifier.tryParse(paramStr);
				if (paramId != null && profile.hasTrait(paramId))
					return true;
				for (Identifier tId : profile.getData().traits())
					if (tId.getPath().equalsIgnoreCase(paramStr) || tId.toString().equalsIgnoreCase(paramStr))
						return true;
				return false;
			}

			@Override
			public ExecutionSide getSide() {
				return ExecutionSide.SERVER;
			}
		});

		register("personality", new ISocialCondition() {
			@Override
			public boolean check(SocialEventContext context, JsonElement params) {
				if (params == null || !params.isJsonPrimitive() || !params.getAsJsonPrimitive().isString())
					return false;
				Optional<VillagiumMob> villagerOpt = context.get(ContextKeys.VILLAGER);
				if (villagerOpt.isEmpty())
					return false;
				Identifier personalityId = villagerOpt.get().getOrCreateProfile().getPersonality().id;
				String paramStr = params.getAsString();
				Identifier paramId = Identifier.tryParse(paramStr);
				return (paramId != null && personalityId.equals(paramId)) 
						|| personalityId.getPath().equalsIgnoreCase(paramStr) 
						|| personalityId.toString().equalsIgnoreCase(paramStr);
			}

			@Override
			public ExecutionSide getSide() {
				return ExecutionSide.SERVER;
			}
		});

		register("weather", new ISocialCondition() {
			@Override
			public boolean check(SocialEventContext context, JsonElement params) {
				if (params == null || !params.isJsonPrimitive() || !params.getAsJsonPrimitive().isString())
					return false;
				Optional<net.minecraft.world.level.Level> levelOpt = context.get(ContextKeys.LEVEL);
				net.minecraft.world.level.Level level = levelOpt.orElseGet(() -> context.get(ContextKeys.VILLAGER).map(mob -> mob.level()).orElse(null));
				if (level == null)
					return false;
				String reqWeather = params.getAsString().toUpperCase(java.util.Locale.ROOT);
				if (reqWeather.equals("RAIN") || reqWeather.equals("RAINY") || reqWeather.equals("THUNDER"))
					return level.isRaining();
				if (reqWeather.equals("CLEAR") || reqWeather.equals("SUNNY"))
					return !level.isRaining();
				return false;
			}

			@Override
			public ExecutionSide getSide() {
				return ExecutionSide.SERVER;
			}
		});
	}
}
