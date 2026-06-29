package com.perengano99.villagium.core.registration;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.items.PingItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Villagium.MODID);
	
	public static final DeferredItem<PingItem> PING_ITEM = ITEMS.registerItem("ping_item", PingItem::new);

	public static final DeferredItem<net.minecraft.world.item.SpawnEggItem> NV_VILLAGER_MALE_SPAWN_EGG = ITEMS.registerItem(
			"nv_villager_male_spawn_egg",
			properties -> {
				net.minecraft.nbt.CompoundTag profileTag = new net.minecraft.nbt.CompoundTag();
				profileTag.putInt("gender", com.perengano99.villagium.social.profile.NvGender.MALE.id);
				net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
				tag.put("profile_data", profileTag);
				// TODO: baby version & culture/localization/relatives context profile selection
				net.minecraft.world.item.component.TypedEntityData<net.minecraft.world.entity.EntityType<?>> entityData =
						net.minecraft.world.item.component.TypedEntityData.of(ModEntityTypes.NV_VILLAGER.get(), tag);
				return new net.minecraft.world.item.SpawnEggItem(
						properties.spawnEgg(ModEntityTypes.NV_VILLAGER.get())
								.component(net.minecraft.core.component.DataComponents.ENTITY_DATA, entityData)
				);
			}
	);

	public static final DeferredItem<net.minecraft.world.item.SpawnEggItem> NV_VILLAGER_FEMALE_SPAWN_EGG = ITEMS.registerItem(
			"nv_villager_female_spawn_egg",
			properties -> {
				net.minecraft.nbt.CompoundTag profileTag = new net.minecraft.nbt.CompoundTag();
				profileTag.putInt("gender", com.perengano99.villagium.social.profile.NvGender.FEMALE.id);
				net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
				tag.put("profile_data", profileTag);
				// TODO: baby version & culture/localization/relatives context profile selection
				net.minecraft.world.item.component.TypedEntityData<net.minecraft.world.entity.EntityType<?>> entityData =
						net.minecraft.world.item.component.TypedEntityData.of(ModEntityTypes.NV_VILLAGER.get(), tag);
				return new net.minecraft.world.item.SpawnEggItem(
						properties.spawnEgg(ModEntityTypes.NV_VILLAGER.get())
								.component(net.minecraft.core.component.DataComponents.ENTITY_DATA, entityData)
				);
			}
	);
	
	public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}

