package com.perengano99.villagium.social.trait.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.profile.NvProfile;
import com.perengano99.villagium.social.trait.Trait;
import com.perengano99.villagium.social.trait.TraitModifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

public record PreferredFoodModifier(
		FoodPreference preference
) implements TraitModifier {

	public static final MapCodec<PreferredFoodModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.xmap(
					s -> FoodPreference.valueOf(s.toUpperCase(Locale.ROOT)),
					Enum::name
			).fieldOf("preference").forGetter(PreferredFoodModifier::preference)
	).apply(instance, PreferredFoodModifier::new));

	@Override
	public Identifier type() {
		return Identifier.fromNamespaceAndPath("villagium", "preferred_food");
	}

	public static Optional<PreferredFoodModifier> getOf(NvProfile profile) {
		return profile.getTraits().stream()
				.map(Trait::modifiers)
				.flatMap(Collection::stream)
				.filter(PreferredFoodModifier.class::isInstance)
				.map(PreferredFoodModifier.class::cast)
				.findFirst();
	}

	public static Optional<ItemStack> findBestFoodInInventory(SimpleContainer inventory, VillagiumMob<?> npc) {
		Optional<PreferredFoodModifier> modifier = getOf(npc.getOrCreateProfile());
		java.util.List<ItemStack> items = new java.util.ArrayList<>();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && stack.get(DataComponents.FOOD) != null)
				items.add(stack);
		}

		if (modifier.isPresent() && modifier.get().preference() == FoodPreference.HIGHEST_NUTRITION)
			return items.stream().max(Comparator.comparingInt(stack -> stack.get(DataComponents.FOOD).nutrition()));
		else
			return items.stream().findFirst();
	}

	@SuppressWarnings("deprecation")
	public static int findBestFoodSlotInContainer(IItemHandler container, VillagiumMob<?> npc) {
		Optional<PreferredFoodModifier> modifier = getOf(npc.getOrCreateProfile());

		if (modifier.isPresent() && modifier.get().preference() == FoodPreference.HIGHEST_NUTRITION)
			return IntStream.range(0, container.getSlots())
					.mapToObj(i -> new SlotFood(i, container.getStackInSlot(i)))
					.filter(sf -> sf.foodProperties() != null)
					.max(Comparator.comparingInt(sf -> sf.foodProperties().nutrition()))
					.map(SlotFood::slot)
					.orElse(-1);
		else
			return IntStream.range(0, container.getSlots())
					.filter(i -> container.getStackInSlot(i).get(DataComponents.FOOD) != null)
					.findFirst()
					.orElse(-1);
	}

	private record SlotFood(int slot, @Nullable FoodProperties foodProperties) {
		SlotFood(int slot, ItemStack stack) {
			this(slot, stack.get(DataComponents.FOOD));
		}
	}

	public enum FoodPreference {
		HIGHEST_NUTRITION
	}
}
