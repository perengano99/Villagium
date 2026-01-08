package com.perengano99.villagium.client;

import com.perengano99.villagium.client.model.BreastModel;
import net.minecraft.world.entity.LivingEntity;

import java.util.WeakHashMap;
import java.util.Map;

// TEMPORAL: Clase temporal para gestión de físicas de pecho.
// TODO: Sincronizar el tick con el de la entidad desde el cliente.
public class BreastPhysicsManager {
	
	// Usamos WeakHashMap para que si la entidad muere, el GC borre sus físicas.
	private static final Map<LivingEntity, BreastModel.Settings> CACHE = new WeakHashMap<>();
	
	public static BreastModel.Settings get(LivingEntity entity) {
		return CACHE.computeIfAbsent(entity, BreastModel.Settings::new);
	}
	
	public static void tick(LivingEntity entity) {
		// Solo tickeamos si ya existe en caché (para no crear basura innecesaria)
		if (CACHE.containsKey(entity)) {
			get(entity).tick();
		}
	}
}