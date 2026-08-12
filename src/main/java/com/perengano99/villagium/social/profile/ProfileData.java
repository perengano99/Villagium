package com.perengano99.villagium.social.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ProfileData(
		int version,
		Component displayName,
		NvGender gender,
		Identifier personalityId,
		Identifier cultureId,
		Set<Identifier> traits,
		AppearanceData appearance,
		String moodId,
		float generalFatigue,
		Map<Identifier, Float> topicFatigue,
		Map<String, Float> moodScores,
		@Nullable VillagiumMob<?> entity
) {
	
	public static final int CURRENT_VERSION = 1;
	
	public static final Identifier UNSPECIFIED_PERSONALITY = Identifier.fromNamespaceAndPath("villagium", "unspecified");
	public static final Identifier UNSPECIFIED_CULTURE = Identifier.fromNamespaceAndPath("villagium", "unspecified");
	public static final Identifier UNSPECIFIED_TRAIT = Identifier.fromNamespaceAndPath("villagium", "unspecified");
	
	public static final MapCodec<ProfileData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.INT.fieldOf("version").orElse(CURRENT_VERSION).forGetter(ProfileData::version),
					ComponentSerialization.CODEC.fieldOf("display_name").orElse(Component.literal("Villager")).forGetter(ProfileData::displayName),
					Codec.INT.xmap(NvGender::fromId, g -> g.id).fieldOf("gender").orElse(NvGender.UNDEFINED).forGetter(ProfileData::gender),
					Identifier.CODEC.fieldOf("personality_id").orElse(UNSPECIFIED_PERSONALITY).forGetter(ProfileData::personalityId),
					Identifier.CODEC.fieldOf("culture_id").orElse(UNSPECIFIED_CULTURE).forGetter(ProfileData::cultureId),
					Identifier.CODEC.listOf().xmap(list -> (Set<Identifier>) new HashSet<>(list), List::copyOf).fieldOf("traits").orElse(new HashSet<>(List.of(UNSPECIFIED_TRAIT)))
							.forGetter(ProfileData::traits),
					AppearanceData.CODEC.fieldOf("appearance").orElse(AppearanceData.defaults()).forGetter(ProfileData::appearance),
					Codec.STRING.fieldOf("mood").orElse(VillagiumData.GENERIC_MOOD_ID).forGetter(ProfileData::moodId),
					Codec.FLOAT.optionalFieldOf("general_fatigue", 0.0f).forGetter(ProfileData::generalFatigue),
					Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).optionalFieldOf("topic_fatigue", Map.of()).forGetter(ProfileData::topicFatigue),
					Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("mood_scores", Map.of()).forGetter(ProfileData::moodScores)
	                                                                                                        )
			.apply(instance, (version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores) -> new ProfileData(
					version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, null
			)));
	
	public static final StreamCodec<FriendlyByteBuf, ProfileData> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull ProfileData decode(FriendlyByteBuf buf) {
			return new ProfileData(
					buf.readInt(),
					ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf),
					NvGender.fromId(buf.readInt()),
					Identifier.STREAM_CODEC.decode(buf),
					Identifier.STREAM_CODEC.decode(buf),
					buf.readCollection(HashSet::new, Identifier.STREAM_CODEC),
					AppearanceData.STREAM_CODEC.decode(buf),
					buf.readUtf(),
					buf.readFloat(),
					buf.readMap(HashMap::new, Identifier.STREAM_CODEC, FriendlyByteBuf::readFloat),
					buf.readMap(HashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readFloat),
					null
			);
		}
		
		@Override
		public void encode(FriendlyByteBuf buf, ProfileData data) {
			buf.writeInt(data.version());
			ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, data.displayName());
			buf.writeInt(data.gender().id);
			Identifier.STREAM_CODEC.encode(buf, data.personalityId());
			Identifier.STREAM_CODEC.encode(buf, data.cultureId());
			buf.writeCollection(data.traits(), Identifier.STREAM_CODEC);
			AppearanceData.STREAM_CODEC.encode(buf, data.appearance());
			buf.writeUtf(data.moodId());
			buf.writeFloat(data.generalFatigue());
			buf.writeMap(data.topicFatigue(), Identifier.STREAM_CODEC, FriendlyByteBuf::writeFloat);
			buf.writeMap(data.moodScores(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeFloat);
		}
	};
	
	public ProfileData(
			int version,
			Component displayName,
			NvGender gender,
			Identifier personalityId,
			Identifier cultureId,
			Set<Identifier> traits,
			AppearanceData appearance) {
		this(version, displayName, gender, personalityId, cultureId, traits, appearance, VillagiumData.GENERIC_MOOD_ID, 0.0f, Map.of(), Map.of(), null);
	}
	
	public static ProfileData defaults() {
		return new ProfileData(
				CURRENT_VERSION,
				Component.literal("Villager"),
				NvGender.UNDEFINED,
				UNSPECIFIED_PERSONALITY,
				UNSPECIFIED_CULTURE,
				new HashSet<>(List.of(UNSPECIFIED_TRAIT)),
				AppearanceData.defaults(),
				VillagiumData.GENERIC_MOOD_ID,
				0.0f,
				Map.of(),
				Map.of(),
				null
		);
	}
	
	public ProfileData withDisplayName(Component displayName) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withGender(NvGender gender) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withPersonalityId(Identifier personalityId) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withCultureId(Identifier cultureId) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withTraits(Set<Identifier> traits) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withAppearance(AppearanceData appearance) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withMoodId(String moodId) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withGeneralFatigue(float generalFatigue) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withTopicFatigue(Map<Identifier, Float> topicFatigue) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withMoodScores(Map<String, Float> moodScores) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public ProfileData withEntity(VillagiumMob<?> entity) {
		return new ProfileData(version, displayName, gender, personalityId, cultureId, traits, appearance, moodId, generalFatigue, topicFatigue, moodScores, entity);
	}
	
	public Mood mood() {
		String activeMood = moodId;
		float maxScore = -Float.MAX_VALUE;
		for (Map.Entry<String, Mood> entry : VillagiumData.MOODS.entrySet()) {
			String id = entry.getKey();
			Mood m = entry.getValue();
			float score = moodScores.getOrDefault(id, 0.0f);
			if (m.persistent())
				score += m.persistentValue();
			if (score > maxScore) {
				maxScore   = score;
				activeMood = id;
			}
		}
		return VillagiumData.getMoodOrDefault(activeMood);
	}
	
	public String getCuid() {
		String name = displayName.getString().replaceAll("[^a-zA-Z]", "");
		if (name.length() < 3)
			name = (name + "NON").substring(0, 3);
		else
			name = name.substring(0, 3);
		
		String father = "Nn";
		String mother = "Nn";
		
		String cultureKey = "GEN";
		Culture culture = VillagiumData.getCulture(cultureId);
		if (culture != null)
			cultureKey = culture.cuidKey();
		
		String village = "Nn";
		
		String birth = "Nn";
		if (entity != null) {
			String bt = entity.getBirthTime();
			if (!bt.isEmpty())
				birth = bt;
		}
		
		String uuidPart = "Nn";
		if (entity != null)
			uuidPart = entity.getUUID().toString().substring(0, 4);
		
		return name.toUpperCase(java.util.Locale.ROOT) +
		       father +
		       mother +
		       cultureKey.toUpperCase(java.util.Locale.ROOT) +
		       village +
		       birth +
		       uuidPart.toUpperCase(java.util.Locale.ROOT);
	}
}
