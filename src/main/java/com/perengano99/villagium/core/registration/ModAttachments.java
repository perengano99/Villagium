package com.perengano99.villagium.core.registration;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.BreastSettings;
import com.perengano99.villagium.social.profile.ProfileData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
	
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Villagium.MODID);
	
	public static final Supplier<AttachmentType<BreastSettings.IBreastPhysics>> BREAST_PHYSICS =
			ATTACHMENTS.register("breast_physics", () -> AttachmentType.builder(() -> BreastSettings.IBreastPhysics.NO_OP).build());
	
	public static final Supplier<AttachmentType<ProfileData>> PROFILE_DATA =
			ATTACHMENTS.register("profile_data", () -> AttachmentType.builder(ProfileData::defaults)
					.serialize(ProfileData.CODEC)
					.sync(ProfileData.STREAM_CODEC)
					.build());
	
	public static void register(IEventBus bus) {
		ATTACHMENTS.register(bus);
	}
}
