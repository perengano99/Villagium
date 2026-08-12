package com.perengano99.villagium.social.interaction;

public enum InteractionOutcome {
	SUCCESS_HIGH,
	SUCCESS_NORMAL,
	NEUTRAL,
	NEUTRAL_FAIL,
	FAILURE_MILD,
	FAILURE_STRONG,
	FAILED_REPETITIVE;

	public boolean isSuccess() {
		return this == SUCCESS_HIGH || this == SUCCESS_NORMAL;
	}

	public boolean isPassed() {
		return this == NEUTRAL || this == NEUTRAL_FAIL || isSuccess();
	}

	public boolean isFailure() {
		return this == FAILURE_MILD || this == FAILURE_STRONG || this == FAILED_REPETITIVE;
	}
}
