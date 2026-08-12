package com.perengano99.villagium.social.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.social.dialogue.DialogueManager;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class NvPersonality {
	public final Identifier id;
	public final Component displayName;
	private final String displayNameKey;

	private final Preferences preferences;
	private final Sensitivities sensitivities;
	private transient DialogueManager dialogueManager;

	public NvPersonality(Identifier id, String displayNameKey, Preferences preferences, Sensitivities sensitivities) {
		this.id = id;
		this.displayNameKey = displayNameKey;
		this.displayName = Component.translatable(displayNameKey);
		this.preferences = preferences != null ? preferences : Preferences.EMPTY;
		this.sensitivities = sensitivities != null ? sensitivities : Sensitivities.EMPTY;
	}

	public DialogueManager getDialogueManager() {
		if (dialogueManager == null)
			dialogueManager = new DialogueManager(this, VillagiumData.getDialoguesOfPersonality(this.id));
		return dialogueManager;
	}

	public String getDisplayNameKey() {
		return displayNameKey;
	}


	public Preferences getPreferences() {
		return preferences;
	}

	public Sensitivities getSensitivities() {
		return sensitivities;
	}

	public record Preferences(Map<String, Double> categories) {
		public static final Preferences EMPTY = new Preferences(Map.of());

		public static final Codec<Preferences> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("categories").forGetter(Preferences::categories)
		).apply(instance, Preferences::new));
	}

	public record Sensitivities(Map<String, Integer> mood, int repetitionPenalty, int repetitionThreshold) {
		public static final Sensitivities EMPTY = new Sensitivities(Map.of(), 0, 2);

		public static final Codec<Sensitivities> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("mood").orElse(Map.of()).forGetter(Sensitivities::mood),
				Codec.INT.fieldOf("repetition_penalty").orElse(0).forGetter(Sensitivities::repetitionPenalty),
				Codec.INT.fieldOf("repetition_threshold").orElse(2).forGetter(Sensitivities::repetitionThreshold)
		).apply(instance, Sensitivities::new));
	}

	private record Display(String name) {
		static final Codec<Display> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				Codec.STRING.fieldOf("name").forGetter(Display::name)
		).apply(inst, Display::new));
	}

	public static final Codec<NvPersonality> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("id", Identifier.fromNamespaceAndPath("villagium", "generic")).forGetter(p -> p.id),
			Display.CODEC.fieldOf("display").xmap(Display::name, Display::new).forGetter(NvPersonality::getDisplayNameKey),
			Preferences.CODEC.optionalFieldOf("preferences", Preferences.EMPTY).forGetter(NvPersonality::getPreferences),
			Sensitivities.CODEC.optionalFieldOf("sensitivities", Sensitivities.EMPTY).forGetter(NvPersonality::getSensitivities)
	).apply(instance, (id, display, prefs, sens) -> new NvPersonality(id, display, prefs, sens)));
}
