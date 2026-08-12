package com.perengano99.villagium.entity.ai.trait;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface TraitBehaviorFactory<T extends TraitBehavior> {
	T create(JsonObject data);
}
