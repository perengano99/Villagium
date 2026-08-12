package com.perengano99.villagium.social.conversation;

import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.condition.ConditionChecker;
import com.perengano99.villagium.social.condition.ISocialCondition;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConversationManager {
	private static final Map<UUID, ConversationSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

	public static void startConversation(ServerPlayer player, VillagiumMob<?> npc) {
		NvProfile profile = npc.getOrCreateProfile();

		if (profile.getData().generalFatigue() > 80.0f) {
			player.sendSystemMessage(Component.literal("§c" + npc.getDisplayName().getString() + " está demasiado cansado para hablar ahora."));
			return;
		}

		float prob = 0.35f;
		RelationshipData rel = profile.getOrCreateRelationshipWith(player.getUUID());
		if (rel != null) {
			float friendship = rel.getValue(RelationshipAxis.FRIENDSHIP);
			prob += friendship / 200.0f;
		}

		if (profile.hasTrait(Identifier.fromNamespaceAndPath("villagium", "outgoing")) || profile.hasTrait(Identifier.fromNamespaceAndPath("villagium", "extrovert")))
			prob += 0.2f;
		if (profile.hasTrait(Identifier.fromNamespaceAndPath("villagium", "introvert")) || profile.hasTrait(Identifier.fromNamespaceAndPath("villagium", "shy")))
			prob -= 0.2f;

		String moodId = profile.getMood().id();
		if (moodId.contains("happy"))
			prob += 0.15f;
		if (moodId.contains("angry") || moodId.contains("sad"))
			prob -= 0.15f;

		prob = Math.max(0.10f, Math.min(0.90f, prob));
		boolean npcInitiates = npc.getRandom().nextFloat() < prob;

		if (npcInitiates) {
			List<SocialEventDefinition> candidates = new ArrayList<>();
			SocialEventContext context = new SocialEventContext();
			context.put(ContextKeys.PLAYER, player);
			context.put(ContextKeys.VILLAGER, npc);

			for (SocialEventDefinition def : VillagiumData.SOCIAL_EVENTS.values()) {
				if (def.type() != null && (def.type().id().getPath().contains("conversation") || def.id().getPath().contains("topic"))) {
					if (ConditionChecker.check(def.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
						float fatigue = profile.getData().topicFatigue().getOrDefault(def.id(), 0.0f);
						if (fatigue < 80.0f)
							candidates.add(def);
					}
				}
			}

			SocialEventDefinition selectedEvent = null;
			if (!candidates.isEmpty())
				selectedEvent = candidates.get(npc.getRandom().nextInt(candidates.size()));
			else
				selectedEvent = VillagiumData.getSocialEvent(Identifier.fromNamespaceAndPath("villagium", "short_topic.weather"));

			if (selectedEvent != null) {
				if (selectedEvent.conversationConfig().isPresent()) {
					ConversationSession session = new ConversationSession(player, npc, selectedEvent, selectedEvent.conversationConfig().get());
					ACTIVE_SESSIONS.put(player.getUUID(), session);
					session.start();
				} else
					com.perengano99.villagium.social.SocialEventManager.handleTrigger(selectedEvent, context);
			}
		} else {
			ConversationSession session = new ConversationSession(player, npc);
			ACTIVE_SESSIONS.put(player.getUUID(), session);
			session.start();
		}
	}

	public static ConversationSession getSession(UUID playerUuid) {
		return ACTIVE_SESSIONS.get(playerUuid);
	}

	public static void endConversation(UUID playerUuid) {
		ACTIVE_SESSIONS.remove(playerUuid);
	}

	public static void registerSession(UUID playerUuid, ConversationSession session) {
		ACTIVE_SESSIONS.put(playerUuid, session);
	}
}
