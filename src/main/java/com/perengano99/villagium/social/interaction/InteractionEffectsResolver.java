package com.perengano99.villagium.social.interaction;

import com.google.gson.JsonObject;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.S2C_SpawnRelationshipAxisParticlePacket;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.condition.ConditionChecker;
import com.perengano99.villagium.social.condition.ISocialCondition;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.social.profile.NvPersonality;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import com.perengano99.villagium.social.relationship.RelationshipData;
import com.perengano99.villagium.social.trait.modifiers.ConditionalEffectModifier;
import com.perengano99.villagium.social.trait.modifiers.ReceptionModifier;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.social.relationship.RelationTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class InteractionEffectsResolver {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Random RND = new Random();

	private InteractionEffectsResolver() {}

	public static void processSocialEvent(SocialEventDefinition eventDef, SocialEventContext context) {
		Optional<Player> playerOpt = context.get(ContextKeys.PLAYER);
		Optional<NvProfile> profileOpt = context.get(ContextKeys.VILLAGER).map(VillagiumMob::getOrCreateProfile);

		if (playerOpt.isEmpty() || profileOpt.isEmpty()) {
			LOGGER.error("Failed to process social event {}: missing player or villager profile.", eventDef.id());
			return;
		}

		ServerPlayer player = (ServerPlayer) playerOpt.get();
		NvProfile profile = profileOpt.get();
		RelationshipData relationship = profile.getOrCreateRelationshipWith(player.getUUID());

		int repetitionCount = profile.recentlyInteractions(eventDef.id());
		float receptionScore = calculateReceptionScore(eventDef, context, profile, relationship, repetitionCount);

		InteractionOutcome outcome = determineOutcome(receptionScore, repetitionCount, profile.getPersonality().getSensitivities().repetitionThreshold());
		context.put(ContextKeys.INTERACTION_OUTCOME, outcome);

		eventDef.type().process(player, profile.getSubject(), eventDef, context, outcome);

		applyEffects(outcome, receptionScore, eventDef, profile, relationship);
		profile.recordInteraction(eventDef.id());
	}

	private static float calculateReceptionScore(SocialEventDefinition eventDef, SocialEventContext context, NvProfile profile, RelationshipData relationship, int repetitionCount) {
		float score = 0f;
		NvPersonality personality = profile.getPersonality();
		Set<String> categories = eventDef.categories();

		double personalityMultiplier = categories.stream()
				.mapToDouble(cat -> personality.getPreferences().categories().getOrDefault(cat, 1.0))
				.reduce(1.0, (a, b) -> a * b);
		score += (float) (eventDef.baseImpact() * personalityMultiplier);
		int tierModifier = 0;
		net.minecraft.resources.Identifier primaryTagId = relationship.getPrimaryTag();
		RelationTag primaryTag = VillagiumData.RELATION_TAGS.get(primaryTagId);
		if (primaryTag != null)
			tierModifier = primaryTag.receptionModifier();
		score += tierModifier;
		score += personality.getSensitivities().mood().getOrDefault(profile.getMood().id(), 0);

		for (var trait : profile.getTraits())
			for (var modifier : trait.modifiers())
				if (modifier instanceof ReceptionModifier(Map<String, Integer> tagScores))
					score += categories.stream().filter(tagScores::containsKey).mapToInt(tagScores::get).sum();
				else if (modifier instanceof ConditionalEffectModifier(Map<String, Float> tagMultipliers, JsonObject conditions))
					if (ConditionChecker.check(conditions, context, ISocialCondition.ExecutionSide.SERVER))
						for (String category : categories)
							score *= tagMultipliers.getOrDefault(category, 1f);

		com.perengano99.villagium.social.profile.Mood mood = profile.getMood();
		if (mood != null)
			for (var modifier : mood.moodModifiers())
				if (modifier instanceof ReceptionModifier(Map<String, Integer> tagScores))
					score += categories.stream().filter(tagScores::containsKey).mapToInt(tagScores::get).sum();
				else if (modifier instanceof ConditionalEffectModifier(Map<String, Float> tagMultipliers, JsonObject conditions))
					if (ConditionChecker.check(conditions, context, ISocialCondition.ExecutionSide.SERVER))
						for (String category : categories)
							score *= tagMultipliers.getOrDefault(category, 1f);

		score += (RND.nextFloat() * 10) - 5;
		return score;
	}

	private static InteractionOutcome determineOutcome(float score, int repetitionCount, int repetitionThreshold) {
		if (score >= 85) return InteractionOutcome.SUCCESS_HIGH;
		if (score >= 65) return InteractionOutcome.SUCCESS_NORMAL;
		if (score >= 40) return InteractionOutcome.NEUTRAL;
		if (score >= 20) return InteractionOutcome.FAILURE_MILD;
		return InteractionOutcome.FAILURE_STRONG;
	}

	private static void applyEffects(InteractionOutcome outcome, float receptionScore, SocialEventDefinition eventDef, NvProfile profile, RelationshipData relationship) {
		if (eventDef.affinityAxes() == null || eventDef.affinityAxes().isEmpty())
			return;

		if (outcome == InteractionOutcome.FAILED_REPETITIVE) {
			eventDef.affinityAxes().forEach((axis, multiplier) -> {
				float change = -0.5f * multiplier;
				relationship.addValue(axis, change);
			});
			relationship.updateRelationshipTags(profile, null, true);
			return;
		}

		float baseChange = (receptionScore - 40) / 10f;
		eventDef.affinityAxes().forEach((axis, multiplier) -> {
			float finalChange = baseChange * multiplier;
			relationship.addValue(axis, finalChange);
			LOGGER.info("Relationship axis {} changed by {} for NPC {}", axis, finalChange, profile.getSubject().getName().getString());

			RelationshipAxis.EffectLevel effectLevel = null;
			if (finalChange > 3.0f) effectLevel = RelationshipAxis.EffectLevel.HIGH_POSITIVE;
			else if (finalChange > 0.1f) effectLevel = RelationshipAxis.EffectLevel.POSITIVE;
			else if (finalChange < -3.0f) effectLevel = RelationshipAxis.EffectLevel.HIGH_NEGATIVE;
			else if (finalChange < -0.1f) effectLevel = RelationshipAxis.EffectLevel.NEGATIVE;

			if (effectLevel != null)
				NetworkManager.PIPELINE.sendToTracking(
						profile.getSubject(),
						new S2C_SpawnRelationshipAxisParticlePacket(profile.getSubject().getId(), axis.getAtlasIndex(effectLevel))
				);
		});
		relationship.updateRelationshipTags(profile, null, true);
	}
}
