package com.perengano99.villagium.social.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record AppearanceData(
		boolean isGenerated,
		Identifier skinId,
		Identifier clothesId,
		Identifier hairId,
		Identifier faceId,
		Identifier toneGroupId,
		int toneIndex
) {
	public static final MapCodec<AppearanceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.fieldOf("is_generated").orElse(false).forGetter(AppearanceData::isGenerated),
			Identifier.CODEC.fieldOf("skin_id").orElse(Identifier.fromNamespaceAndPath("villagium", "skin_female_default")).forGetter(AppearanceData::skinId),
			Identifier.CODEC.fieldOf("clothes_id").orElse(Identifier.fromNamespaceAndPath("villagium", "clothes_female_default")).forGetter(AppearanceData::clothesId),
			Identifier.CODEC.fieldOf("hair_id").orElse(Identifier.fromNamespaceAndPath("villagium", "hair_female_default")).forGetter(AppearanceData::hairId),
			Identifier.CODEC.fieldOf("face_id").orElse(Identifier.fromNamespaceAndPath("villagium", "face_female_default")).forGetter(AppearanceData::faceId),
			Identifier.CODEC.fieldOf("tone_group_id").orElse(Identifier.fromNamespaceAndPath("villagium", "generic")).forGetter(AppearanceData::toneGroupId),
			Codec.INT.fieldOf("tone_index").orElse(0).forGetter(AppearanceData::toneIndex)
	).apply(instance, AppearanceData::new));

	public static final StreamCodec<FriendlyByteBuf, AppearanceData> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull AppearanceData decode(FriendlyByteBuf buf) {
			return new AppearanceData(
					buf.readBoolean(),
					Identifier.STREAM_CODEC.decode(buf),
					Identifier.STREAM_CODEC.decode(buf),
					Identifier.STREAM_CODEC.decode(buf),
					Identifier.STREAM_CODEC.decode(buf),
					Identifier.STREAM_CODEC.decode(buf),
					buf.readVarInt()
			);
		}

		@Override
		public void encode(FriendlyByteBuf buf, AppearanceData data) {
			buf.writeBoolean(data.isGenerated());
			Identifier.STREAM_CODEC.encode(buf, data.skinId());
			Identifier.STREAM_CODEC.encode(buf, data.clothesId());
			Identifier.STREAM_CODEC.encode(buf, data.hairId());
			Identifier.STREAM_CODEC.encode(buf, data.faceId());
			Identifier.STREAM_CODEC.encode(buf, data.toneGroupId());
			buf.writeVarInt(data.toneIndex());
		}
	};

	public static AppearanceData defaults() {
		return new AppearanceData(
				false,
				Identifier.fromNamespaceAndPath("villagium", "skin_female_default"),
				Identifier.fromNamespaceAndPath("villagium", "clothes_female_default"),
				Identifier.fromNamespaceAndPath("villagium", "hair_female_default"),
				Identifier.fromNamespaceAndPath("villagium", "face_female_default"),
				Identifier.fromNamespaceAndPath("villagium", "generic"),
				0
		);
	}
}
