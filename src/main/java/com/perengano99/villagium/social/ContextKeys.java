package com.perengano99.villagium.social;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.social.context.ContextKey;
import com.perengano99.villagium.social.interaction.InteractionOutcome;
import com.perengano99.villagium.entity.VillagiumMob;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ContextKeys {
	private ContextKeys() {}

	public static final ContextKey<Level> LEVEL = new ContextKey<>(path("level"));
	public static final ContextKey<Player> PLAYER = new ContextKey<>(path("player"));
	public static final ContextKey<VillagiumMob> VILLAGER = new ContextKey<>(path("villager"));
	public static final ContextKey<ItemStack> HELD_ITEM = new ContextKey<>(path("held_item"));
	public static final ContextKey<Float> DAMAGE_AMOUNT = new ContextKey<>(path("damage_amount"));
	public static final ContextKey<ItemStack> GIFTED_ITEM = new ContextKey<>(path("gifted_item"));
	public static final ContextKey<InteractionOutcome> INTERACTION_OUTCOME = new ContextKey<>(path("outcome"));
	public static final ContextKey<String> CONVERSATION_TYPE = new ContextKey<>(path("conversation_type"));

	private static Identifier path(String path) {
		return Identifier.fromNamespaceAndPath(Villagium.MODID, path);
	}
}
