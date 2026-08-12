package com.perengano99.villagium.social.profile;

import com.perengano99.villagium.data.NamesLoader;
import net.minecraft.network.chat.Component;
import java.util.Random;

public class NvProfileFactory {
	
	private static final Random rnd = new Random();
	
	private NvProfileFactory() {}
	
	private static Component generateNewName(NvGender gender) {
		return Component.literal(NamesLoader.getRandomName("generic", gender));
	}
	
	public static void generateNewProfile(NvProfile profile) {
		ProfileData current = profile.getData();
		ProfileData defs = ProfileData.defaults();
		
		NvGender gender = current.gender() == NvGender.UNDEFINED ? (rnd.nextBoolean() ? NvGender.FEMALE : NvGender.MALE) : current.gender();
		Component name = current.displayName().getString().equals(defs.displayName().getString()) ? generateNewName(gender) : current.displayName();
		
		net.minecraft.resources.Identifier personality = current.personalityId().equals(ProfileData.UNSPECIFIED_PERSONALITY)
				? com.perengano99.villagium.data.VillagiumData.getRandomPersonality().id
				: current.personalityId();
				
		net.minecraft.resources.Identifier cultureId = current.cultureId().equals(ProfileData.UNSPECIFIED_CULTURE)
				? com.perengano99.villagium.data.VillagiumData.getRandomCulture().id()
				: current.cultureId();
				
		String culture = cultureId.getPath();
		
		java.util.Set<net.minecraft.resources.Identifier> traits;
		if (current.traits().contains(ProfileData.UNSPECIFIED_TRAIT)) {
			traits = new java.util.HashSet<>();
			int count = rnd.nextInt(2) + 1;
			for (com.perengano99.villagium.social.trait.Trait t : com.perengano99.villagium.data.VillagiumData.getRandomTraits(count))
				traits.add(t.id());
		} else
			traits = current.traits();
		
		AppearanceData appearance;
		if (current.appearance().isGenerated())
			appearance = current.appearance();
		else {
			com.perengano99.villagium.data.TonesLoader.ServerToneResult tone = com.perengano99.villagium.data.TonesLoader.getRandomToneIndex(culture);
			net.minecraft.resources.Identifier skin = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("skin", gender, culture);
			net.minecraft.resources.Identifier clothes = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("clothes", gender, culture);
			net.minecraft.resources.Identifier hair = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("hair", gender, culture);
			net.minecraft.resources.Identifier face = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("face", gender, culture);
			
			appearance = new AppearanceData(
					true,
					skin,
					clothes,
					hair,
					face,
					tone.group(),
					tone.index()
			);
		}
		
		ProfileData newData = new ProfileData(
				ProfileData.CURRENT_VERSION,
				name,
				gender,
				personality,
				cultureId,
				traits,
				appearance
		);
		
		profile.setData(newData);
	}
	
	public static void regenerateIdentity(NvProfile profile, boolean changeName, boolean changeGender) {
		ProfileData currentData = profile.getData();
		
		NvGender gender = currentData.gender();
		Component name = currentData.displayName();
		
		if (changeGender) {
			gender = gender == NvGender.MALE ? NvGender.FEMALE : NvGender.MALE;
			name = generateNewName(gender);
		} else if (changeName)
			name = generateNewName(gender);
		
		String culture = currentData.cultureId().getPath();
		
		com.perengano99.villagium.data.TonesLoader.ServerToneResult tone = com.perengano99.villagium.data.TonesLoader.getRandomToneIndex(culture);
		net.minecraft.resources.Identifier skin = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("skin", gender, culture);
		net.minecraft.resources.Identifier clothes = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("clothes", gender, culture);
		net.minecraft.resources.Identifier hair = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("hair", gender, culture);
		net.minecraft.resources.Identifier face = com.perengano99.villagium.data.AppearanceLoader.getRandomTextureId("face", gender, culture);
		
		AppearanceData appearance = new AppearanceData(
				true,
				skin,
				clothes,
				hair,
				face,
				tone.group(),
				tone.index()
		);
		
		profile.setData(currentData
				.withDisplayName(name)
				.withGender(gender)
				.withAppearance(appearance));
	}
	
	public static void regenerateName(NvProfile profile) {
		Component newName = generateNewName(profile.getGender());
		profile.setName(newName);
	}
}
