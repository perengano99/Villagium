package com.perengano99.villagium.social.event.events;

import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.S2C_UpdateDialoguePacket;
import com.perengano99.villagium.social.ContextKeys;
import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.conversation.ConversationSession;
import com.perengano99.villagium.social.conversation.ServerConversationManager;
import com.perengano99.villagium.social.dialogue.DialogueEntry;
import com.perengano99.villagium.social.dialogue.DialogueManager;
import com.perengano99.villagium.social.event.SocialEvent;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.social.interaction.InteractionOutcome;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import java.util.Optional;

public record ConversationEvent(Identifier id) implements SocialEvent {
	private static final Logger LOGGER = Logger.getLogger();

	@Override
	public void process(LivingEntity trigger, VillagiumMob<?> villager, SocialEventDefinition eventDefinition, SocialEventContext context, InteractionOutcome outcome) {
		// TODO (AI-Based Memory System):
		// Integrate SensorType and MemoryModuleType to feed current conversation details into the mob's brain.
		// Use a custom Sensor to detect nearby conversation participants and record active topics/outcomes in Minecraft Brain memories.
		// Temporary memories (e.g. recent conversation logs) will be stored in MemoryModuleType.CONVERSATION_MEMORY.

		if (trigger instanceof ServerPlayer player) {
			if (eventDefinition.conversationConfig().isPresent()) {
				ConversationSession session = new ConversationSession(player, villager, eventDefinition, eventDefinition.conversationConfig().get());
				ServerConversationManager.registerSession(player.getUUID(), session);
				session.start();
			} else {
				NvProfile profile = villager.getOrCreateProfile();
				DialogueManager dialogueManager = profile.getPersonality().getDialogueManager();
				String dialogueType = context.get(ContextKeys.CONVERSATION_TYPE).orElse("event_reaction");

				Optional<DialogueEntry> selectedEntry = dialogueManager.selectDialogue(dialogueType, eventDefinition.id().toString(), context);
				String responseKey = selectedEntry.map(dialogueManager::getRandomTranslatableKey)
						.orElseGet(() -> {
							LOGGER.warn("No dialogue entry found for event '{}' with outcome '{}'.", eventDefinition.id(), outcome.name());
							return "dialogue.villagium.generic.fallback_response";
						});

				NetworkManager.PIPELINE.sendToClient(player, new S2C_UpdateDialoguePacket(responseKey));
			}
		} else if (trigger instanceof VillagiumMob<?> otherNpc) {
			// TODO (NPC-NPC Conversation System):
			// Skip the full multi-turn screen conversation flow.
			// Run optimized instant simulation of dialogue, calculate relationship shifts directly based on both NPC traits/moods.
			// Spawn visual heart/anger particles above NPCs to provide feedback to nearby players.
		}
	}
}
