package com.perengano99.villagium.social.context;

import net.minecraft.resources.Identifier;

public final class ContextKey<T> {
	private final Identifier name;

	public ContextKey(Identifier name) {
		this.name = name;
	}

	public Identifier name() {
		return name;
	}

	@Override
	public String toString() {
		return "ContextKey[" + name + "]";
	}
}
