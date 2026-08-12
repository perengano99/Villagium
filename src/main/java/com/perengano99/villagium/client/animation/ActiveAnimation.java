package com.perengano99.villagium.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.world.entity.AnimationState;
import org.jetbrains.annotations.Nullable;

public class ActiveAnimation {
	
	public enum Phase {
		INTRO,
		LOOP,
		OUTRO
	}
	
	private final ModelAnimation animation;
	private final AnimationState state = new AnimationState();
	private final Boolean loopOverride;
	private final float speedFactor;
	private final boolean isManual;
	private Phase phase;
	private int ticksElapsed;
	private int phaseTicksElapsed;
	
	public ActiveAnimation(ModelAnimation animation) {
		this(animation, null, 1.0f, false);
	}
	
	public ActiveAnimation(ModelAnimation animation, @Nullable Boolean loopOverride) {
		this(animation, loopOverride, 1.0f, false);
	}

	public ActiveAnimation(ModelAnimation animation, @Nullable Boolean loopOverride, float speedFactor, boolean isManual) {
		this.animation = animation;
		this.loopOverride = loopOverride;
		this.speedFactor = speedFactor;
		this.isManual = isManual;
		if (animation.getIntro() != null) {
			this.phase = Phase.INTRO;
		} else if (animation.getLoop() != null) {
			this.phase = Phase.LOOP;
		} else {
			this.phase = Phase.OUTRO;
		}
	}
	
	public float getSpeedFactor() {
		return speedFactor;
	}
	
	public boolean isManual() {
		return isManual;
	}
	
	public ModelAnimation getAnimation() {
		return animation;
	}
	
	public AnimationState getState() {
		return state;
	}
	
	public Phase getPhase() {
		return phase;
	}
	
	public void setPhase(Phase phase) {
		this.phase = phase;
		this.phaseTicksElapsed = 0;
	}
	
	public int getTicksElapsed() {
		return ticksElapsed;
	}
	
	public int getPhaseTicksElapsed() {
		return phaseTicksElapsed;
	}
	
	public AnimationDefinition getCurrentDefinition() {
		if (phase == null) return null;
		return switch (phase) {
			case INTRO -> animation.getIntro();
			case LOOP -> animation.getLoop();
			case OUTRO -> animation.getOutro();
		};
	}
	
	public void tick(int tickCount) {
		ticksElapsed++;
		phaseTicksElapsed++;
		
		AnimationDefinition current = getCurrentDefinition();
		if (current == null) {
			advancePhase(tickCount);
			return;
		}
		
		if (phase == Phase.INTRO || phase == Phase.OUTRO) {
			int durationTicks = (int) (current.lengthInSeconds() * 20.0f);
			if (phaseTicksElapsed >= durationTicks) {
				advancePhase(tickCount);
			}
		}
	}
	
	private void advancePhase(int tickCount) {
		if (phase == Phase.INTRO) {
			if (animation.getLoop() != null) {
				setPhase(Phase.LOOP);
				state.start(tickCount);
			} else if (animation.getOutro() != null) {
				setPhase(Phase.OUTRO);
				state.start(tickCount);
			} else {
				phase = null;
			}
		} else if (phase == Phase.LOOP) {
			boolean shouldLoop = (loopOverride != null) ? loopOverride : animation.isLoop();
			if (shouldLoop) {
				phaseTicksElapsed = 0;
			} else {
				if (animation.getOutro() != null) {
					setPhase(Phase.OUTRO);
					state.start(tickCount);
				} else {
					phase = null;
				}
			}
		} else if (phase == Phase.OUTRO) {
			phase = null;
		}
	}
	
	public void stopOrOutro(int tickCount) {
		if (phase == Phase.INTRO || phase == Phase.LOOP) {
			if (animation.getOutro() != null) {
				setPhase(Phase.OUTRO);
				state.start(tickCount);
			} else {
				phase = null;
			}
		}
	}
	
	public void restart(int tickCount) {
		ticksElapsed = 0;
		phaseTicksElapsed = 0;
		if (animation.getIntro() != null) {
			this.phase = Phase.INTRO;
		} else if (animation.getLoop() != null) {
			this.phase = Phase.LOOP;
		} else {
			this.phase = Phase.OUTRO;
		}
		state.start(tickCount);
	}
	
	public boolean isFinished() {
		return phase == null;
	}
}
