package com.perengano99.villagium.social.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.relationship.RelationshipAxis;
import com.perengano99.villagium.social.relationship.RelationshipData;
import com.perengano99.villagium.social.trait.Trait;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NvProfile {

	private final VillagiumMob<?> subject;
	private final Map<UUID, RelationshipData> relationships = new HashMap<>();
	private final Set<UUID> knownEntities = new HashSet<>();
	private final Cache<Identifier, AtomicInteger> recentInteractions = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	public NvProfile(VillagiumMob<?> subject) {
		this.subject = subject;
	}

	public Mood getMood() {
		return getData().mood();
	}

	public void setMood(Mood mood) {
		if (mood != null)
			setMood(mood.id());
	}

	public void setMood(String moodId) {
		ProfileData current = getData();
		if (!current.moodId().equals(moodId))
			setData(current.withMoodId(moodId));
	}

	public ProfileData getData() {
		ProfileData data = subject.getData(ModAttachments.PROFILE_DATA.get());
		if (data.entity() != subject) {
			data = data.withEntity(subject);
			subject.setData(ModAttachments.PROFILE_DATA.get(), data);
		}
		return data;
	}

	public void setData(ProfileData newData) {
		if (newData.entity() != subject)
			newData = newData.withEntity(subject);
		subject.setData(ModAttachments.PROFILE_DATA.get(), newData);
		if (!subject.level().isClientSide()) {
			subject.setCustomName(newData.displayName());
			subject.syncAppearanceToTracking();
		}
	}

	public Component getDisplayName() {
		return getData().displayName();
	}

	public NvGender getGender() {
		return getData().gender();
	}

	@NotNull
	public NvPersonality getPersonality() {
		return VillagiumData.getPersonalityOfId(getData().personalityId());
	}

	@NotNull
	public Culture getCulture() {
		Culture culture = VillagiumData.getCulture(getData().cultureId());
		if (culture == null)
			culture = VillagiumData.getRandomCulture();
		return culture;
	}

	public Set<Trait> getTraits() {
		return getData().traits().stream()
				.map(VillagiumData::getTrait)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public void setName(Component name) {
		ProfileData current = getData();
		if (!Objects.equals(current.displayName(), name))
			setData(current.withDisplayName(name));
	}

	public void setGender(NvGender gender) {
		ProfileData current = getData();
		if (current.gender() != gender)
			setData(current.withGender(gender));
	}

	public void setPersonality(@NotNull NvPersonality personality) {
		ProfileData current = getData();
		if (!current.personalityId().equals(personality.id)) {
			setData(current.withPersonalityId(personality.id));
			updateAllRelationships();
		}
	}

	public void setCulture(@NotNull Culture culture) {
		ProfileData current = getData();
		if (!current.cultureId().equals(culture.id()))
			setData(current.withCultureId(culture.id()));
	}

	public void addTrait(Trait trait) {
		if (trait != null) {
			Set<Identifier> newTraits = new HashSet<>(getData().traits());
			if (newTraits.add(trait.id()))
				setData(getData().withTraits(newTraits));
		}
	}

	public void removeTrait(Trait trait) {
		if (trait != null) {
			Set<Identifier> newTraits = new HashSet<>(getData().traits());
			if (newTraits.remove(trait.id()))
				setData(getData().withTraits(newTraits));
		}
	}

	public boolean hasTrait(Trait trait) {
		return trait != null && getData().traits().contains(trait.id());
	}

	public boolean hasTrait(Identifier traitId) {
		return traitId != null && getData().traits().contains(traitId);
	}

	public void recordInteraction(Identifier eventId) {
		try {
			recentInteractions.get(eventId, () -> new AtomicInteger(0)).getAndIncrement();
		} catch (ExecutionException e) {
			recentInteractions.put(eventId, new AtomicInteger(1));
		}
	}

	public int recentlyInteractions(Identifier eventId) {
		AtomicInteger count = recentInteractions.getIfPresent(eventId);
		return count != null ? count.get() : 0;
	}

	public Map<UUID, RelationshipData> getRelationships() {
		return Collections.unmodifiableMap(relationships);
	}

	@Nullable
	public RelationshipData getRelationWith(UUID targetUuid) {
		return relationships.get(targetUuid);
	}

	public Set<UUID> getKnownEntities() {
		return knownEntities;
	}

	public RelationshipData getOrCreateRelationshipWith(UUID targetUuid) {
		return relationships.computeIfAbsent(targetUuid, k -> {
			RelationshipData newRel = new RelationshipData(k);
			knownEntities.add(k);
			newRel.updateRelationshipTags(this, null, true);
			return newRel;
		});
	}

	public void setRelationship(RelationshipData data) {
		relationships.put(data.getTargetUuid(), data);
	}

	public void clearRelationships() {
		this.relationships.clear();
	}

	public void updateAllRelationships() {
		for (RelationshipData data : relationships.values())
			data.updateRelationshipTags(this, null, knownEntities.contains(data.getTargetUuid()));
	}

	public void applyDailyDecayAndCleanup() {
		float baseDecayRate = 0.05f;

		for (RelationshipData rel : relationships.values()) {
			for (RelationshipAxis axis : RelationshipAxis.values()) {
				float targetVal = 0f;
				float decayRate = baseDecayRate;

				if (axis == RelationshipAxis.RESENTMENT) {
					if (hasTrait(Identifier.fromNamespaceAndPath("villagium", "grudge_holder")))
						decayRate = 0f;
				}

				rel.applyDecay(axis, targetVal, decayRate);
			}

			if (hasTrait(Identifier.fromNamespaceAndPath("villagium", "grudge_holder"))) {
				float resentment = rel.getValue(RelationshipAxis.RESENTMENT);
				if (resentment > 30f)
					rel.applyDecay(RelationshipAxis.FRIENDSHIP, -50f, 0.1f);
			}

			rel.updateRelationshipTags(this, null, true);
		}

		relationships.values().removeIf(rel -> {
			if (!rel.getTemporaryModifiers().isEmpty())
				return false;

			boolean allNeutral = true;
			for (RelationshipAxis axis : RelationshipAxis.values()) {
				if (Math.abs(rel.getValue(axis)) > 5.0f) {
					allNeutral = false;
					break;
				}
			}
			return allNeutral;
		});

		float currentGeneralFatigue = getData().generalFatigue();
		float newGeneralFatigue = Math.max(0.0f, currentGeneralFatigue - 50.0f);

		Map<Identifier, Float> currentTopicFatigue = new HashMap<>(getData().topicFatigue());
		currentTopicFatigue.replaceAll((id, fatigue) -> Math.max(0.0f, fatigue - 20.0f));
		currentTopicFatigue.values().removeIf(val -> val <= 0.0f);

		Map<String, Float> currentMoodScores = new HashMap<>(getData().moodScores());
		currentMoodScores.replaceAll((id, score) -> score * 0.8f);
		currentMoodScores.values().removeIf(val -> val <= 0.0f);

		setData(getData()
				.withGeneralFatigue(newGeneralGeneralFatigue(newGeneralFatigue))
				.withTopicFatigue(currentTopicFatigue)
				.withMoodScores(currentMoodScores));
	}

	private float newGeneralGeneralFatigue(float fatigue) {
		return fatigue;
	}

	public float getGeneralFatigue() {
		return getData().generalFatigue();
	}

	public void setGeneralFatigue(float fatigue) {
		setData(getData().withGeneralFatigue(Math.min(100.0f, Math.max(0.0f, fatigue))));
	}

	public float getTopicFatigue(Identifier topicId) {
		return getData().topicFatigue().getOrDefault(topicId, 0.0f);
	}

	public void changeMoodScore(String moodId, float amount) {
		Map<String, Float> scores = new HashMap<>(getData().moodScores());
		float current = scores.getOrDefault(moodId, 0.0f);
		float next = Math.max(0.0f, current + amount);
		if (next <= 0.0f)
			scores.remove(moodId);
		else
			scores.put(moodId, next);
		setData(getData().withMoodScores(scores));
	}

	public void forceMood(String moodId) {
		Map<String, Float> scores = new HashMap<>();
		scores.put(moodId, 100.0f);
		setData(getData().withMoodScores(scores));
	}

	public void tickRelationships() {
		if (subject.level().isClientSide())
			return;

		boolean tagsChanged = false;
		for (RelationshipData rel : relationships.values()) {
			if (rel.tickModifiers())
				tagsChanged = true;
		}
		if (tagsChanged)
			updateAllRelationships();

		long gameTime = subject.level().getGameTime();
		int staggerOffset = Math.abs(subject.getUUID().hashCode()) % 24000;
		if ((gameTime + staggerOffset) % 24000 == 0)
			applyDailyDecayAndCleanup();
	}

	public VillagiumMob<?> getSubject() {
		return subject;
	}
}
