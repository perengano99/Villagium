package com.perengano99.villagium.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class BakedAnimationHolder {
	
	private final Map<ModelPart, Map<String, KeyframeAnimation>> bakedCache = new IdentityHashMap<>();
	private boolean dirty = true;
	
	public void invalidate() {
		this.dirty = true;
	}
	
	public KeyframeAnimation getBaked(String animId, AnimationDefinition definition, ModelPart root) {
		if (dirty) {
			bakedCache.clear();
			dirty = false;
		}
		Map<String, KeyframeAnimation> modelCache = bakedCache.computeIfAbsent(root, r -> new HashMap<>());
		return modelCache.computeIfAbsent(animId, id -> {
			return definition != null ? definition.bake(root) : null;
		});
	}
}
