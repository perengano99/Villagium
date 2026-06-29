package com.perengano99.villagium.entity.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.entity.ai.trait.TraitBehavior;
import com.perengano99.villagium.entity.ai.trait.TraitBehaviorRegistry;
import com.perengano99.villagium.social.trait.modifiers.AddBehaviorModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class VillagiumMobAi<T extends VillagiumMob<T>> {

	protected final Map<Activity, ImmutableList<Pair<Integer, ? extends BehaviorControl<? super T>>>> behaviors;

	protected VillagiumMobAi(T npc) {
		Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super T>>>> mutables = buildBehaviors(npc);

		if (!npc.level().isClientSide()) {
			Map<Activity, List<Pair<? extends BehaviorControl<? super T>, Integer>>> traitBehaviors = new HashMap<>();
			npc.getOrCreateProfile().getTraits().forEach(t -> {
				t.modifiers().forEach(m -> {
					if (m instanceof AddBehaviorModifier modifier)
						applyTraitBehavior(modifier, traitBehaviors);
				});
			});

			for (var entry : traitBehaviors.entrySet()) {
				entry.getValue().add(Pair.of(new DoNothing(30, 60), 1));
				mutables.computeIfAbsent(entry.getKey(), n -> new ArrayList<>()).add(Pair.of(3, new RunOne<>(entry.getValue())));
			}
		}

		this.behaviors = mutables.entrySet().stream().collect(
				Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())));
	}

	protected abstract Map<Activity, List<Pair<Integer, ? extends BehaviorControl<? super T>>>> buildBehaviors(T npc);

	public ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super T>>> getBehaviors(Activity activity) {
		return behaviors.get(activity);
	}

	@SuppressWarnings("unchecked")
	private void applyTraitBehavior(AddBehaviorModifier modifier, Map<Activity, List<Pair<? extends BehaviorControl<? super T>, Integer>>> behaviors) {
		Identifier behaviorId = modifier.behavior();
		TraitBehaviorRegistry.get(behaviorId).ifPresent(factory -> {
			TraitBehavior behavior = factory.create(modifier.data());
			BehaviorControl<? super T> newBehavior = (BehaviorControl<? super T>) behavior.getBehavior();
			behaviors.computeIfAbsent(parseActivity(modifier.activity()), n -> new ArrayList<>()).add(Pair.of(newBehavior, modifier.priority()));
		});
	}

	private Activity parseActivity(Identifier id) {
		return BuiltInRegistries.ACTIVITY.get(id).map(net.minecraft.core.Holder::value).orElse(Activity.IDLE);
	}
}
