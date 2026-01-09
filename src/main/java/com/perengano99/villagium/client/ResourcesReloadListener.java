package com.perengano99.villagium.client;

import com.perengano99.villagium.client.renderer.HslTextureBaker;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

public class ResourcesReloadListener extends SimplePreparableReloadListener<Void> {
	
	@Override
	protected Void prepare(@NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
		return null;
	}
	
	@Override
	protected void apply(Void unused, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
		HslTextureBaker.clearCache();
	}
}
