package com.perengano99.villagium.social;

import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.social.condition.ConditionChecker;
import com.perengano99.villagium.social.condition.ISocialCondition;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.interaction.InteractionEffectsResolver;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class SocialEventManager {
	private SocialEventManager() {}

	public static void handleTrigger(@NotNull SocialEventDefinition eventDefinition, SocialEventContext context) {
		if (ConditionChecker.check(eventDefinition.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
			SocialEventDefinition resolved = resolveVariant(eventDefinition, context);
			InteractionEffectsResolver.processSocialEvent(resolved, context);
		}
	}

	private static SocialEventDefinition resolveVariant(SocialEventDefinition definition, SocialEventContext context) {
		if (definition.variants().isEmpty())
			return definition;
		for (SocialEventDefinition variant : definition.variants().get().values())
			if (ConditionChecker.check(variant.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER))
				return variant;
		return definition;
	}

	public static void handleBroadcastTrigger(Identifier triggerTypeId, SocialEventContext context) {
		for (SocialEventDefinition eventDef : VillagiumData.SOCIAL_EVENTS.values())
			if (eventDef.type() != null && eventDef.type().id().equals(triggerTypeId))
				if (ConditionChecker.check(eventDef.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
					SocialEventDefinition resolved = resolveVariant(eventDef, context);
					InteractionEffectsResolver.processSocialEvent(resolved, context);
				}
	}
}
