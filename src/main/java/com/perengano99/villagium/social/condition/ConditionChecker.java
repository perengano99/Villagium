package com.perengano99.villagium.social.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perengano99.villagium.social.context.SocialEventContext;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ConditionChecker {

	public static boolean check(@Nullable JsonObject conditionsJson, SocialEventContext context, ISocialCondition.ExecutionSide side) {
		if (conditionsJson == null)
			return true;

		for (Map.Entry<String, JsonElement> entry : conditionsJson.entrySet()) {
			String conditionId = entry.getKey();
			ISocialCondition condition = ConditionRegistry.get(conditionId);
			if (condition == null)
				continue;

			if (side == ISocialCondition.ExecutionSide.CLIENT && condition.getSide() == ISocialCondition.ExecutionSide.SERVER)
				return false;

			if (!condition.check(context, entry.getValue()))
				return false;
		}

		return true;
	}
}
