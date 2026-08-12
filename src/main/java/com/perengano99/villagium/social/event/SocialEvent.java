package com.perengano99.villagium.social.event;

import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.interaction.InteractionOutcome;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public interface SocialEvent {
	Identifier id();

	void process(LivingEntity trigger, VillagiumMob<?> villager, SocialEventDefinition eventDefinition, SocialEventContext context, InteractionOutcome outcome);
}
