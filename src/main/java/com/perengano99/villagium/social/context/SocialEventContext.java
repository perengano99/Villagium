package com.perengano99.villagium.social.context;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

public final class SocialEventContext {
	private final Map<ContextKey<?>, Object> data = Maps.newIdentityHashMap();

	public <T> void put(ContextKey<T> key, T value) {
		this.data.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(ContextKey<T> key) {
		return Optional.ofNullable((T) this.data.get(key));
	}

	public boolean has(ContextKey<?> key) {
		return this.data.containsKey(key);
	}
}
