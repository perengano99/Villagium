package com.perengano99.villagium.items;

import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.PingPongPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class PingItem extends Item {
	
	public PingItem(Properties properties) {
		super(properties);
	}
	
	@Override
	public @NonNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget,
	                                                       @NotNull InteractionHand hand) {
		if (player.level().isClientSide()){
			NetworkManager.PIPELINE.sendToServer(new PingPongPacket());
			return  InteractionResult.SUCCESS;
		} else {
			player.displayClientMessage(player.getDisplayName(), true);
		}
		return InteractionResult.PASS;
	}
}
