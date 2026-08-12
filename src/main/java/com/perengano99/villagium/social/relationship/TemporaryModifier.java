package com.perengano99.villagium.social.relationship;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TemporaryModifier(
		String id,
		Optional<RelationshipAxis> axis,
		float axisAmount,
		Optional<Identifier> tagId,
		int tagWeight,
		int remainingTicks
) {
	public static final Codec<TemporaryModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(TemporaryModifier::id),
			RelationshipAxis.CODEC.optionalFieldOf("axis").forGetter(TemporaryModifier::axis),
			Codec.FLOAT.optionalFieldOf("axis_amount", 0f).forGetter(TemporaryModifier::axisAmount),
			Identifier.CODEC.optionalFieldOf("tag_id").forGetter(TemporaryModifier::tagId),
			Codec.INT.optionalFieldOf("tag_weight", 0).forGetter(TemporaryModifier::tagWeight),
			Codec.INT.fieldOf("remaining_ticks").forGetter(TemporaryModifier::remainingTicks)
	).apply(instance, TemporaryModifier::new));

	public static final StreamCodec<FriendlyByteBuf, TemporaryModifier> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull TemporaryModifier decode(FriendlyByteBuf buf) {
			return new TemporaryModifier(
					buf.readUtf(),
					buf.readOptional(b -> b.readEnum(RelationshipAxis.class)),
					buf.readFloat(),
					buf.readOptional(Identifier.STREAM_CODEC::decode),
					buf.readVarInt(),
					buf.readVarInt()
			);
		}

		@Override
		public void encode(FriendlyByteBuf buf, TemporaryModifier modifier) {
			buf.writeUtf(modifier.id());
			buf.writeOptional(modifier.axis(), (b, val) -> b.writeEnum(val));
			buf.writeFloat(modifier.axisAmount());
			buf.writeOptional(modifier.tagId(), Identifier.STREAM_CODEC::encode);
			buf.writeVarInt(modifier.tagWeight());
			buf.writeVarInt(modifier.remainingTicks());
		}
	};

	public TemporaryModifier tick() {
		return new TemporaryModifier(id, axis, axisAmount, tagId, tagWeight, Math.max(0, remainingTicks - 1));
	}

	public boolean isExpired() {
		return remainingTicks <= 0;
	}
}
