package com.perengano99.villagium.client.animations;

import com.perengano99.villagium.client.animations.face.FaceModelController;
import com.perengano99.villagium.client.renderer.state.BreastPhysicsState;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.entity.BreastSettings;
import net.minecraft.world.entity.LivingEntity;

import java.util.WeakHashMap;
import java.util.Map;

// TEMPORAL: Clase temporal para gestión de físicas de pecho.
// TODO: Sincronizar el tick con el de la entidad desde el cliente.
public class BreastPhysicsManager {
	
	// Usamos WeakHashMap para que si la entidad muere, el GC borre sus físicas.
	private static final Map<LivingEntity, BreastSettings> CACHE = new WeakHashMap<>();
	private static final Map<LivingEntity, FaceModelController<?>> FACE_CACHE = new WeakHashMap<>();
	
	public static BreastSettings get(LivingEntity entity) {
		return CACHE.computeIfAbsent(entity, k -> new BreastSettings());
	}
	
	public static FaceModelController<?> getFaceController(LivingEntity entity) {
		return FACE_CACHE.computeIfAbsent(entity, k -> new FaceModelController<>());
	}
	
	public static void tick(LivingEntity entity) {
		// Solo tickeamos si ya existe en caché (para no crear basura innecesaria)
		BreastSettings.IBreastPhysics physics = entity.getData(ModAttachments.BREAST_PHYSICS);
		if (physics == BreastSettings.IBreastPhysics.NO_OP) {
			BreastPhysicsState state = new BreastPhysicsState();
			entity.setData(ModAttachments.BREAST_PHYSICS, state);
			physics = state;
		}
		physics.tick(entity, get(entity));
	}
}