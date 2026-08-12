package com.perengano99.villagium.client.registration;

import com.perengano99.villagium.client.gui.screens.InteractionScreen;
import com.perengano99.villagium.client.gui.screens.NpcMenuScreen;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.entity.interaction.MenuNpc;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class NpcMenuRegistry {
	
	private static final Map<EntityType<? extends VillagiumMob<?>>, ScreenFactory> REGISTRY = new HashMap<>();
	
	
	public static <T extends VillagiumMob<?> & MenuNpc> void register(EntityType<@NonNull T> entity, ScreenFactory factory) {
		REGISTRY.put(entity, factory);
	}
	
	public static @Nullable NpcMenuScreen<?> createScreen(EntityType<? extends VillagiumMob<?>> entity, MenuDataPacket payload) {
		ScreenFactory factory = REGISTRY.get(entity);
		return factory != null ? factory.create(payload) : null;
	}
	
	public interface ScreenFactory {
		
		NpcMenuScreen<?> create(MenuDataPacket payload);
	}
	
	public interface MenuDataPacket {
		
		int entityId();
	}
}
