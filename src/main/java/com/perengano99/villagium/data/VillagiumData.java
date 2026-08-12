package com.perengano99.villagium.data;

import com.google.common.collect.ImmutableMap;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.core.util.logging.Logger;
import com.perengano99.villagium.social.dialogue.DialogueEntry;
import com.perengano99.villagium.social.event.SocialEventDefinition;
import com.perengano99.villagium.social.profile.Culture;
import com.perengano99.villagium.social.profile.NvPersonality;
import com.perengano99.villagium.social.relationship.RelationTag;
import com.perengano99.villagium.social.trait.Trait;
import com.perengano99.villagium.social.profile.Mood;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VillagiumData {
	private static final Logger LOGGER = Logger.getLogger();
	private static final Random RND = new Random();

	public static final Identifier GENERIC_PERSONALITY_ID = Identifier.fromNamespaceAndPath(Villagium.MODID, "generic");
	public static final Identifier GENERIC_CULTURE_ID = Identifier.fromNamespaceAndPath(Villagium.MODID, "generic");
	public static final String GENERIC_MOOD_ID = "neutral";

	public static final Map<Identifier, Culture> CULTURES = new HashMap<>();
	public static final Map<Identifier, NvPersonality> PERSONALITIES = new HashMap<>();
	public static final Map<Identifier, Trait> TRAITS = new HashMap<>();
	public static final Map<Identifier, Map<String, DialogueEntry>> DIALOGUES = new HashMap<>();
	public static final Map<Identifier, SocialEventDefinition> SOCIAL_EVENTS = new HashMap<>();
	public static final Map<Identifier, RelationTag> RELATION_TAGS = new HashMap<>();
	public static final Map<String, Mood> MOODS = new HashMap<>();

	private VillagiumData() {}

	@NotNull
	public static Mood getMoodOrDefault(@Nullable String id) {
		Mood mood = null;
		if (id != null)
			mood = MOODS.get(id);
		if (mood == null)
			mood = MOODS.get(GENERIC_MOOD_ID);
		if (mood == null) {
			mood = new Mood(GENERIC_MOOD_ID, "mood.neutral");
			MOODS.put(GENERIC_MOOD_ID, mood);
		}
		return mood;
	}

	@Nullable
	public static SocialEventDefinition getSocialEvent(Identifier id) {
		return SOCIAL_EVENTS.get(id);
	}

	@Nullable
	public static Culture getCulture(Identifier id) {
		return CULTURES.get(id);
	}

	@NotNull
	public static Culture getRandomCulture() {
		List<Culture> list = CULTURES.values().stream().filter(c -> !c.id().equals(GENERIC_CULTURE_ID)).toList();
		if (!list.isEmpty())
			return list.get(RND.nextInt(list.size()));
		Culture generic = CULTURES.get(GENERIC_CULTURE_ID);
		if (generic == null) {
			generic = new Culture(GENERIC_CULTURE_ID, "culture.villagium.generic", "GEN", Culture.CivilizationParams.defaults(), Culture.Rules.defaults());
			CULTURES.put(GENERIC_CULTURE_ID, generic);
		}
		return generic;
	}

	@Nullable
	public static Trait getTrait(Identifier id) {
		return TRAITS.get(id);
	}

	@NotNull
	public static Set<Trait> getRandomTraits(int count) {
		if (TRAITS.isEmpty() || count <= 0)
			return Collections.emptySet();

		List<Trait> pool = new ArrayList<>(TRAITS.values());
		int numToPick = Math.min(count, pool.size());
		Collections.shuffle(pool);
		return new HashSet<>(pool.subList(0, numToPick));
	}

	public static Map<String, DialogueEntry> getDialoguesOfPersonality(Identifier personalityId) {
		Map<String, DialogueEntry> dialogueMap = DIALOGUES.get(personalityId);
		if (dialogueMap == null)
			return ImmutableMap.of();
		return ImmutableMap.copyOf(dialogueMap);
	}

	@NotNull
	public static NvPersonality getPersonalityOfId(@Nullable Identifier id) {
		NvPersonality personality = null;
		if (id != null)
			personality = PERSONALITIES.get(id);

		if (personality == null) {
			if (id != null && !id.equals(GENERIC_PERSONALITY_ID))
				LOGGER.warn("Personality with ID '{}' not found. Falling back to generic.", id);
			personality = PERSONALITIES.get(GENERIC_PERSONALITY_ID);
		}

		if (personality == null) {
			LOGGER.error("CRITICAL FALLBACK: Generic personality is null or not loaded! Creating in-memory fallback.");
			personality = new NvPersonality(GENERIC_PERSONALITY_ID, "personality.villagium.generic.displayname", NvPersonality.Preferences.EMPTY, NvPersonality.Sensitivities.EMPTY);
			PERSONALITIES.put(GENERIC_PERSONALITY_ID, personality);
		}
		return personality;
	}

	@NotNull
	public static NvPersonality getRandomPersonality() {
		List<NvPersonality> pool = PERSONALITIES.values().stream().filter(p -> !p.id.equals(GENERIC_PERSONALITY_ID)).toList();
		if (!pool.isEmpty())
			return pool.get(RND.nextInt(pool.size()));
		return getPersonalityOfId(GENERIC_PERSONALITY_ID);
	}
}
