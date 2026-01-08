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
	
	public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
