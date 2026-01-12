package com.perengano99.villagium.entity.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class VillagiumMobAi<T extends VillagiumMob<T>> {
	
	protected final Map<Activity, ImmutableList<Pair<Integer, ? extends BehaviorControl<? super T>>>> behaviors;
	
	protected VillagiumMobAi(T npc) {
		Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super T>>>> mutables = buildBehaviors(npc);
		
		this.behaviors = mutables.entrySet().stream().collect(
				Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())));
	}
	
	protected abstract Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super T>>>> buildBehaviors(T npc);
	
	public ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super T>>> getBehaviors(Activity activity) {
		return behaviors.get(activity);
	}
}
