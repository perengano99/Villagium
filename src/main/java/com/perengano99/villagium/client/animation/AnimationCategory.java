package com.perengano99.villagium.client.animation;

public enum AnimationCategory {
	IDLE(0),
	POSE(1),
	MOVEMENT(2),
	ACTION(3);

	private final int priority;

	AnimationCategory(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}
