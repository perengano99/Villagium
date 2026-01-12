package com.perengano99.villagium.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.perengano99.villagium.entity.npc.NvVillager;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.schedule.Activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NvVillagerAi extends VillagiumMobAi<NvVillager> {
	
	public NvVillagerAi(NvVillager npc) {
		super(npc);
	}
	
	@Override
	protected Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super NvVillager>>>> buildBehaviors(NvVillager npc) {
		Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super NvVillager>>>> map = new HashMap<>();
		
		map.put(Activity.CORE, Lists.newArrayList(
				Pair.of(0, new LookAtTargetSink(45, 90)),
				Pair.of(1, new MoveToTargetSink())));
		
		map.put(Activity.IDLE, Lists.newArrayList(
				Pair.of(1, SetEntityLookTarget.create(EntityType.PLAYER, 6)),
				Pair.of(2, new RunOne<>(
						ImmutableList.of(
								Pair.of(RandomStroll.stroll(0.5f), 4),
								Pair.of(new RandomLookAround(UniformInt.of(150, 250), 70, 0, 0), 3),
								Pair.of(SetWalkTargetFromLookTarget.create(0.5f, 2), 2),
								Pair.of(new DoNothing(20, 60), 1))))));
		
		return map;
	}
}
