package com.perengano99.villagium.social.conversation;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.network.packets.S2C_UpdateConversationPacket;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.condition.ConditionChecker;
import com.perengano99.villagium.social.condition.ISocialCondition;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.social.profile.Mood;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import com.perengano99.villagium.social.relationship.RelationshipData;
import com.perengano99.villagium.social.relationship.TemporaryModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class ConversationSession {
	
	private final ServerPlayer player;
	private final VillagiumMob<?> npc;
	private SocialEventDefinition eventDef;
	private ConversationConfig config;
	private String currentNodeId;
	
	private List<String> currentCategories = new ArrayList<>();
	private List<SocialEventDefinition> currentCategoryTopics = new ArrayList<>();
	
	private final Map<RelationshipAxis, Float> relationshipChanges = new HashMap<>();
	private final List<String> addedTags = new ArrayList<>();
	private final List<String> removedTags = new ArrayList<>();
	private final String initialMoodId;
	private final Identifier initialPrimaryTagId;
	private String currentMoodId;
	private float totalFatigueChange = 0.0f;
	private float topicFatigueAdded = 0.0f;
	
	public ConversationSession(ServerPlayer player, VillagiumMob<?> npc, SocialEventDefinition eventDef, ConversationConfig config) {
		this.player        = player;
		this.npc           = npc;
		this.eventDef      = eventDef;
		this.config        = config;
		this.currentNodeId = config.startNodeId();
		
		NvProfile profile = npc.getOrCreateProfile();
		this.initialMoodId = profile.getMood().id();
		this.currentMoodId = this.initialMoodId;
		
		RelationshipData rel = profile.getOrCreateRelationshipWith(player.getUUID());
		this.initialPrimaryTagId = rel.getPrimaryTag();
	}
	
	public ConversationSession(ServerPlayer player, VillagiumMob<?> npc) {
		this.player        = player;
		this.npc           = npc;
		this.eventDef      = null;
		this.config        = null;
		this.currentNodeId = "__player_initiate__";
		
		NvProfile profile = npc.getOrCreateProfile();
		this.initialMoodId = profile.getMood().id();
		this.currentMoodId = this.initialMoodId;
		
		RelationshipData rel = profile.getOrCreateRelationshipWith(player.getUUID());
		this.initialPrimaryTagId = rel.getPrimaryTag();
	}
	
	public void start() {
		sendCurrentNodeToClient();
	}
	
	public void selectChoice(int choiceIndex) {
		if (currentNodeId.equals("__player_initiate__")) {
			if (choiceIndex >= 0 && choiceIndex < currentCategories.size())
				selectCategory(currentCategories.get(choiceIndex));
			else
				endConversation();
			return;
		}
		
		if (currentNodeId.equals("__category_topics__")) {
			if (choiceIndex >= 0 && choiceIndex < currentCategoryTopics.size())
				selectTopic(currentCategoryTopics.get(choiceIndex).id());
			else
				endConversation();
			return;
		}
		
		ConversationNode node = config.nodes().get(currentNodeId);
		if (node == null || choiceIndex < 0 || choiceIndex >= node.choices().size()) {
			endConversation();
			return;
		}
		
		ConversationChoice choice = node.choices().get(choiceIndex);
		applyChoiceEffects(choice);
		
		if (choice.nextNodeId().isPresent() && !choice.nextNodeId().get().isEmpty()) {
			currentNodeId = choice.nextNodeId().get();
			sendCurrentNodeToClient();
		} else
			endConversation();
	}
	
	public void selectCategory(String cat) {
		SocialEventContext context = new SocialEventContext();
		context.put(ContextKeys.PLAYER, player);
		context.put(ContextKeys.VILLAGER, npc);
		
		NvProfile profile = npc.getOrCreateProfile();
		List<SocialEventDefinition> matches = new ArrayList<>();
		for (SocialEventDefinition def : VillagiumData.SOCIAL_EVENTS.values()) {
			if (def.type() != null && (def.type().id().getPath().contains("conversation") || def.id().getPath().contains("topic"))) {
				if (def.categories().contains(cat)) {
					if (ConditionChecker.check(def.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
						float fatigue = profile.getData().topicFatigue().getOrDefault(def.id(), 0.0f);
						if (fatigue < 80.0f)
							matches.add(def);
					}
				}
			}
		}
		
		if (matches.size() > 5)
			matches = matches.subList(0, 5);
		if (matches.size() < 2) {
			SocialEventDefinition weather = VillagiumData.getSocialEvent(Identifier.fromNamespaceAndPath("villagium", "short_topic.weather"));
			if (weather != null && !matches.contains(weather))
				matches.add(weather);
			SocialEventDefinition joke = VillagiumData.getSocialEvent(Identifier.fromNamespaceAndPath("villagium", "short_topic.joke"));
			if (joke != null && !matches.contains(joke))
				matches.add(joke);
		}
		
		this.currentCategoryTopics = matches;
		this.currentNodeId         = "__category_topics__";
		sendCurrentNodeToClient();
	}
	
	public void selectTopic(Identifier topicId) {
		SocialEventDefinition chosenEvent = VillagiumData.getSocialEvent(topicId);
		if (chosenEvent != null) {
			this.eventDef = chosenEvent;
			if (chosenEvent.conversationConfig().isPresent()) {
				this.config        = chosenEvent.conversationConfig().get();
				this.currentNodeId = config.startNodeId();
				sendCurrentNodeToClient();
			} else {
				SocialEventContext context = new SocialEventContext();
				context.put(ContextKeys.PLAYER, player);
				context.put(ContextKeys.VILLAGER, npc);
				com.perengano99.villagium.social.SocialEventManager.handleTrigger(chosenEvent, context);
				endConversation();
			}
		} else
			endConversation();
	}
	
	public void cancelConversation() {
		ServerConversationManager.endConversation(player.getUUID());
		
		NvProfile profile = npc.getOrCreateProfile();
		if (eventDef != null && eventDef.cancellationEffects().isPresent()) {
			ConversationCancellationEffects effects = eventDef.cancellationEffects().get();
			effects.relationshipEffects().ifPresent(relEffects -> {
				RelationshipData relationship = profile.getOrCreateRelationshipWith(player.getUUID());
				relEffects.forEach((axis, amount) -> {
					relationshipChanges.put(axis, relationshipChanges.getOrDefault(axis, 0.0f) + amount);
					relationship.addValue(axis, amount);
				});
			});
			totalFatigueChange += effects.fatigueCost();
		}
		
		profile.setData(profile.getData().withGeneralFatigue(
				Math.clamp(profile.getData().generalFatigue() + totalFatigueChange, 0.0f, 100.0f)));
		
		profile.recordInteraction(eventDef != null ? eventDef.id() : Identifier.fromNamespaceAndPath(Villagium.MODID, "conversation"));
		profile.updateAllRelationships();
		
		sendChatFeedback();
		
		List<Identifier> activeRelationTags = new ArrayList<>();
		RelationshipData relation = profile.getRelationWith(player.getUUID());
		if (relation != null)
			activeRelationTags.addAll(relation.getActiveTags().keySet().stream().sorted().limit(3).toList());
//		NetworkManager.PIPELINE.sendToClient(player, new S2C_OpenInteractMenuPacket(npc.getId(), npc.getOrCreateProfile().getData(), activeRelationTags));
	}
	
	private void applyChoiceEffects(ConversationChoice choice) {
		NvProfile profile = npc.getOrCreateProfile();
		RelationshipData relationship = profile.getOrCreateRelationshipWith(player.getUUID());
		
		choice.relationshipEffects().ifPresent(effects -> {
			effects.forEach((axis, amount) -> {
				relationshipChanges.put(axis, relationshipChanges.getOrDefault(axis, 0.0f) + amount);
				relationship.addValue(axis, amount);
			});
		});
		
		choice.forceMoodId().ifPresent(moodId -> {
			currentMoodId = moodId;
			profile.forceMood(moodId);
		});
		
		choice.moodModifiers().ifPresent(modifiers -> {
			modifiers.forEach(profile::changeMoodScore);
			currentMoodId = profile.getMood().id();
		});
		
		choice.addTags().ifPresent(tags -> {
			for (String tagStr : tags) {
				Identifier tId = Identifier.tryParse(tagStr);
				if (tId != null) {
					addedTags.add(tagStr);
					relationship.addTemporaryModifier(new TemporaryModifier(
							"conv_add_" + tagStr + "_" + npc.getRandom().nextInt(1000),
							Optional.empty(),
							0.0f,
							Optional.of(tId),
							50,
							24000
					));
				}
			}
		});
		
		choice.removeTags().ifPresent(tags -> {
			for (String tagStr : tags) {
				Identifier tId = Identifier.tryParse(tagStr);
				if (tId != null) {
					removedTags.add(tagStr);
					relationship.addTemporaryModifier(new TemporaryModifier(
							"conv_rem_" + tagStr + "_" + npc.getRandom().nextInt(1000),
							Optional.empty(),
							0.0f,
							Optional.of(tId),
							-50,
							24000
					));
				}
			}
		});
		
		totalFatigueChange += choice.fatigueCost();
		topicFatigueAdded += choice.fatigueCost();
	}
	
	private void sendCurrentNodeToClient() {
		if (currentNodeId.equals("__player_initiate__")) {
			Set<String> uniqueCategories = new LinkedHashSet<>();
			SocialEventContext context = new SocialEventContext();
			context.put(ContextKeys.PLAYER, player);
			context.put(ContextKeys.VILLAGER, npc);
			
			NvProfile profile = npc.getOrCreateProfile();
			for (SocialEventDefinition def : VillagiumData.SOCIAL_EVENTS.values()) {
				if (def.type() != null && (def.type().id().getPath().contains("conversation") || def.id().getPath().contains("topic"))) {
					if (ConditionChecker.check(def.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
						float fatigue = profile.getData().topicFatigue().getOrDefault(def.id(), 0.0f);
						if (fatigue < 80.0f)
							uniqueCategories.addAll(def.categories());
					}
				}
			}
			
			List<String> categories = uniqueCategories.stream()
					.filter(cat -> !cat.equals("conversation_topic") && !cat.equals("neutral") && !cat.equals("social") && !cat.equals("light"))
					.limit(3)
					.toList();
			
			if (categories.isEmpty())
				categories = List.of("weather", "humor", "social");
			
			this.currentCategories = categories;
			
			List<S2C_UpdateConversationPacket.ChoiceClientData> choicesList = new ArrayList<>();
			for (int i = 0; i < categories.size(); i++) {
				String cat = categories.get(i);
				choicesList.add(new S2C_UpdateConversationPacket.ChoiceClientData(i, "gui.villagium.category." + cat, true));
			}
			
			NetworkManager.PIPELINE.sendToClient(player, new S2C_UpdateConversationPacket(
					npc.getId(),
					Identifier.fromNamespaceAndPath(Villagium.MODID, "conversation"),
					"__player_initiate__",
					Component.translatable("dialogue.villagium.player_initiate_prompt"),
					choicesList
			));
			return;
		}
		
		if (currentNodeId.equals("__category_topics__")) {
			List<S2C_UpdateConversationPacket.ChoiceClientData> choicesList = new ArrayList<>();
			for (int i = 0; i < currentCategoryTopics.size(); i++) {
				SocialEventDefinition def = currentCategoryTopics.get(i);
				String displayKey = def.displayKey().orElse("villagium.topic.generic");
				choicesList.add(new S2C_UpdateConversationPacket.ChoiceClientData(i, displayKey, true));
			}
			
			NetworkManager.PIPELINE.sendToClient(player, new S2C_UpdateConversationPacket(
					npc.getId(),
					Identifier.fromNamespaceAndPath("villagium", "conversation"),
					"__category_topics__",
					Component.translatable("dialogue.villagium.category_topics_prompt"),
					choicesList
			));
			return;
		}
		
		ConversationNode node = config.nodes().get(currentNodeId);
		if (node == null) {
			endConversation();
			return;
		}
		
		SocialEventContext context = new SocialEventContext();
		context.put(ContextKeys.PLAYER, player);
		context.put(ContextKeys.VILLAGER, npc);
		
		ConversationLine bestLine = null;
		for (ConversationLine line : node.lines()) {
			if (ConditionChecker.check(line.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER)) {
				bestLine = line;
				break;
			}
		}
		if (bestLine == null && !node.lines().isEmpty())
			bestLine = node.lines().getFirst();
		
		String lineKey = "dialogue.villagium.generic.fallback_response";
		if (bestLine != null && !bestLine.translationKeys().isEmpty()) {
			List<String> keys = bestLine.translationKeys();
			lineKey = keys.get(npc.getRandom().nextInt(keys.size()));
		}
		
		List<S2C_UpdateConversationPacket.ChoiceClientData> choicesList = new ArrayList<>();
		for (int i = 0; i < node.choices().size(); i++) {
			ConversationChoice choice = node.choices().get(i);
			boolean available = ConditionChecker.check(choice.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER);
			choicesList.add(new S2C_UpdateConversationPacket.ChoiceClientData(i, choice.textKey(), available));
		}
		
		NetworkManager.PIPELINE.sendToClient(player, new S2C_UpdateConversationPacket(
				npc.getId(),
				eventDef.id(),
				currentNodeId,
				Component.translatable(lineKey),
				choicesList
		));
	}
	
	private void endConversation() {
		ServerConversationManager.endConversation(player.getUUID());
		
		NvProfile profile = npc.getOrCreateProfile();
		profile.setData(profile.getData().withGeneralFatigue(
				Math.clamp(profile.getData().generalFatigue() + totalFatigueChange, 0.0f, 100.0f)));
		
		if (eventDef != null) {
			Map<Identifier, Float> currentTopicFatigues = new HashMap<>(profile.getData().topicFatigue());
			float newTopicFatigue = currentTopicFatigues.getOrDefault(eventDef.id(), 0.0f) + topicFatigueAdded;
			currentTopicFatigues.put(eventDef.id(), Math.clamp(newTopicFatigue, 0.0f, 100.0f));
			profile.setData(profile.getData().withTopicFatigue(currentTopicFatigues));
			profile.recordInteraction(eventDef.id());
		}
		
		profile.updateAllRelationships();
		sendChatFeedback();
		
		List<Identifier> activeRelationTags = new ArrayList<>();
		RelationshipData relation = profile.getRelationWith(player.getUUID());
		if (relation != null)
			activeRelationTags.addAll(relation.getActiveTags().keySet().stream().sorted().limit(3).toList());
		NetworkManager.PIPELINE.sendToClient(player, new S2C_OpenInteractMenuPacket(npc.getId(), npc.getOrCreateProfile().getData(), activeRelationTags));
	}
	
	private void sendChatFeedback() {
		player.sendSystemMessage(Component.literal("§7[Conversación finalizada con " + npc.getDisplayName().getString() + "]"));
		
		if (!relationshipChanges.isEmpty()) {
			relationshipChanges.forEach((axis, change) -> {
				String sign = change >= 0 ? "+" : "";
				player.sendSystemMessage(Component.literal(" §8- §e" + axis.name() + ": §a" + sign + String.format("%.1f", change)));
			});
		}
		
		if (!initialMoodId.equals(currentMoodId)) {
			Mood mood = VillagiumData.getMoodOrDefault(currentMoodId);
			player.sendSystemMessage(Component.literal(" §8- §eHumor: §f" + Component.translatable(mood.displayKey()).getString()));
		}
		
		RelationshipData rel = npc.getOrCreateProfile().getRelationWith(player.getUUID());
		if (rel != null) {
			Identifier finalPrimaryTagId = rel.getPrimaryTag();
			if (!initialPrimaryTagId.equals(finalPrimaryTagId)) {
				String oldTag = initialPrimaryTagId.getPath();
				String newTag = finalPrimaryTagId.getPath();
				player.sendSystemMessage(Component.literal(" §8- §eRelación: §d" + oldTag + " -> " + newTag));
			}
		}
		
		if (totalFatigueChange > 0)
			player.sendSystemMessage(Component.literal(" §8- §eFatiga del NPC: §c+" + String.format("%.1f", totalFatigueChange)));
	}
}
