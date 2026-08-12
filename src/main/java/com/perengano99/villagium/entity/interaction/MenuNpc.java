package com.perengano99.villagium.entity.interaction;

import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for interactable entities.
 * Implement on mob classes (e.g., NvVillager) to enable the right-click interaction menu.
 */
public interface MenuNpc {
	
	/**
	 * Checks if player can interact and open the screen.
	 */
	default boolean canOpenMenu(Player player) {
		return true;
	}
	
	/**
	 * Try to open the attached screen to VillagiumEntity in Client Side sending a open packet.
	 */
	<T extends VillagiumMob<T>> void tryOpenMenu(T source, Player player);
}
