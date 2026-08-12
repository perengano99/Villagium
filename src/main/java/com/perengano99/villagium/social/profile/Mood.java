package com.perengano99.villagium.social.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.social.trait.TraitModifier;

import java.util.ArrayList;
import java.util.List;

public class Mood {
	private final String id;
	private final String displayKey;
	private final List<TraitModifier> moodModifiers;
	private final String displayColor;
	private final boolean display;

	private final boolean persistent;
	private final float persistentValue;

	public Mood(String id, String displayKey, List<TraitModifier> moodModifiers, String displayColor, boolean display, boolean persistent, float persistentValue) {
		this.id = id;
		this.displayKey = displayKey;
		this.moodModifiers = moodModifiers != null ? new ArrayList<>(moodModifiers) : new ArrayList<>();
		this.displayColor = displayColor != null ? displayColor : "#020202";
		this.display = display;
		this.persistent = persistent;
		this.persistentValue = persistentValue;
	}

	public Mood(String id, String displayKey, List<TraitModifier> moodModifiers, String displayColor, boolean display) {
		this(id, displayKey, moodModifiers, displayColor, display, false, 0.0f);
	}

	public Mood(String id, String displayKey, List<TraitModifier> moodModifiers, String displayColor) {
		this(id, displayKey, moodModifiers, displayColor, true, false, 0.0f);
	}

	public Mood(String id, String displayKey, List<TraitModifier> moodModifiers) {
		this(id, displayKey, moodModifiers, "#020202", true, false, 0.0f);
	}

	public Mood(String id, String displayKey) {
		this(id, displayKey, List.of(), "#020202", true, false, 0.0f);
	}

	public String id() {
		return id;
	}

	public String displayKey() {
		return displayKey;
	}

	public List<TraitModifier> moodModifiers() {
		return moodModifiers;
	}

	public String displayColor() {
		return displayColor;
	}

	public boolean display() {
		return display;
	}

	public boolean persistent() {
		return persistent;
	}

	public float persistentValue() {
		return persistentValue;
	}

	public void addModifiers(List<TraitModifier> modifiers) {
		if (modifiers != null)
			this.moodModifiers.addAll(modifiers);
	}

	public int getDisplayColorInt() {
		int color = 0xFF020202;
		if (displayColor != null && !displayColor.isEmpty())
			try {
				String colorStr = displayColor;
				if (colorStr.startsWith("#"))
					colorStr = colorStr.substring(1);
				if (colorStr.length() == 6)
					color = 0xFF000000 | Integer.parseInt(colorStr, 16);
				else
					color = (int) Long.parseLong(colorStr, 16);
			} catch (Exception e) {
				// Fallback
			}
		return color;
	}

	public static final Codec<Mood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(Mood::id),
			Codec.STRING.fieldOf("display_key").forGetter(Mood::displayKey),
			TraitModifier.CODEC.listOf().optionalFieldOf("mood_modifiers", List.of()).forGetter(Mood::moodModifiers),
			Codec.STRING.optionalFieldOf("display_color", "#020202").forGetter(Mood::displayColor),
			Codec.BOOL.optionalFieldOf("display", true).forGetter(Mood::display),
			Codec.BOOL.optionalFieldOf("persistent", false).forGetter(Mood::persistent),
			Codec.FLOAT.optionalFieldOf("persistent_value", 0.0f).forGetter(Mood::persistentValue)
	).apply(instance, Mood::new));
}
