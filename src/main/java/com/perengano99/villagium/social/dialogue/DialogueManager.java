package com.perengano99.villagium.social.dialogue;

import com.perengano99.villagium.social.context.SocialEventContext;
import com.perengano99.villagium.social.condition.ConditionChecker;
import com.perengano99.villagium.social.condition.ISocialCondition;
import com.perengano99.villagium.social.profile.NvPersonality;
import com.perengano99.villagium.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DialogueManager {
	private static final Logger LOGGER = Logger.getLogger();

	private final NvPersonality personality;
	private final Map<String, DialogueEntry> dialogueIdMap;
	private final Map<String, List<DialogueEntry>> dialogueTypeMap;
	private final Random rnd = new Random();

	public DialogueManager(@NotNull NvPersonality personality, @NotNull Map<String, DialogueEntry> personalityDialogues) {
		this.personality = personality;
		Objects.requireNonNull(personalityDialogues, "Personality dialogues map cannot be null");

		this.dialogueIdMap   = personalityDialogues;
		this.dialogueTypeMap = this.dialogueIdMap.values().stream().collect(Collectors.groupingBy(DialogueEntry::type));
	}

	public String getRandomTranslatableKey(@NotNull DialogueEntry entry) {
		if (entry.translationKeys() == null || entry.translationKeys().isEmpty()) {
			LOGGER.error("DialogueEntry con ID '{}' no tiene claves de traducción.", entry.id());
			return "dialogue.villagium.error.missing_key";
		}
		return entry.translationKeys().get(rnd.nextInt(entry.translationKeys().size()));
	}

	public DialogueEntry getDialogueEntry(String id) {
		return dialogueIdMap.get(id);
	}

	public Optional<DialogueEntry> selectDialogue(@NotNull final String type, @Nullable final String subtype, @NotNull SocialEventContext context) {
		List<DialogueEntry> candidates = dialogueTypeMap.getOrDefault(type, Collections.emptyList());
		if (candidates.isEmpty())
			return Optional.empty();

		candidates = candidates.stream().filter(entry -> {
			if (subtype != null)
				return entry.event().map(st -> st.equalsIgnoreCase(subtype)).orElse(false);
			return entry.event().isEmpty();
		}).toList();

		if (candidates.isEmpty())
			return Optional.empty();

		List<DialogueEntry> validByCondition = new ArrayList<>();
		for (DialogueEntry entry : candidates)
			if (ConditionChecker.check(entry.conditions().orElse(null), context, ISocialCondition.ExecutionSide.SERVER))
				validByCondition.add(entry);

		if (validByCondition.isEmpty())
			return Optional.empty();
		candidates = validByCondition;

		final int maxSpecificity = candidates.stream().mapToInt(DialogueEntry::specificity).max().orElse(0);
		candidates = candidates.stream().filter(entry -> entry.specificity() == maxSpecificity).toList();

		return Optional.ofNullable(selectByWeight(candidates));
	}

	private @Nullable DialogueEntry selectByWeight(@NotNull List<DialogueEntry> entries) {
		if (entries.isEmpty())
			return null;
		if (entries.size() == 1)
			return entries.getFirst();

		int totalWeight = entries.stream().mapToInt(DialogueEntry::weight).sum();
		if (totalWeight <= 0)
			return entries.get(rnd.nextInt(entries.size()));

		int randomNumber = rnd.nextInt(totalWeight);
		int cumulativeWeight = 0;
		for (DialogueEntry entry : entries) {
			cumulativeWeight += entry.weight();
			if (randomNumber < cumulativeWeight)
				return entry;
		}
		return entries.getLast();
	}
}
