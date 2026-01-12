package com.perengano99.villagium.client.animations.face;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;

public interface IFaceModelAnimation<S extends NvHumanoidRenderState> {
	
	void tick(FaceModelAnimator<S> animator, long gameTime);
	
	void animate(FaceModelAnimator<S> animator, float partialTicks);
}
