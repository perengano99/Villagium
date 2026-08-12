package com.perengano99.villagium.client.network;

import com.perengano99.villagium.client.VillagiumClient;
import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.client.gui.screens.ConversationScreen;
import com.perengano99.villagium.client.gui.screens.InteractionScreen;
import com.perengano99.villagium.client.gui.screens.NpcMenuScreen;
import com.perengano99.villagium.client.registration.AnimationRegistry;
import com.perengano99.villagium.client.animation.ModelAnimationController;
import com.perengano99.villagium.client.animation.ModelAnimation;
import com.perengano99.villagium.client.animation.TempAnimManager;
import com.perengano99.villagium.client.registration.NpcMenuRegistry;
import com.perengano99.villagium.client.registration.NpcMenuRegistry.MenuDataPacket;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.packets.S2C_OpenInteractMenuPacket;
import com.perengano99.villagium.network.packets.S2C_UpdateConversationPacket;
import com.perengano99.villagium.network.packets.SyncAppearanceToClientPacket;
import com.perengano99.villagium.network.packets.SyncMobAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public class ClientPacketHandler {
	
	public static void handleSyncMobAnimation(SyncMobAnimationPacket payload) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		Entity entity = level.getEntity(payload.entityId());
		if (entity instanceof VillagiumMob<?> mob) {
			ModelAnimationController controller = TempAnimManager.getAnimationController(mob);
			if (controller == null) return;
			
			if (payload.start()) {
				Optional<ModelAnimation> animOpt = AnimationRegistry.getAnimationById(payload.animationId());
				if (animOpt.isPresent()) {
					ModelAnimation anim = animOpt.get();
					boolean loop = anim.isLoop();
					if (!anim.isLoop()) {
						if (payload.loopMode() == 0)
							loop = false;
						else if (payload.loopMode() == 1)
							loop = true;
					}
					
					int duration = payload.durationTicks();
					float speed = payload.speedFactor();
					
					controller.play(anim, loop, duration, speed, true);
				}
			} else {
				if (payload.animationId().isEmpty()) {
					for (AnimationCategory cat : AnimationCategory.values())
						controller.stop(cat);
				} else {
					Optional<ModelAnimation> animOpt = AnimationRegistry.getAnimationById(payload.animationId());
					animOpt.ifPresent(modelAnimation -> controller.stop(modelAnimation.getCategory()));
				}
			}
		}
	}
	
	public static void handleSyncAppearance(SyncAppearanceToClientPacket payload) {
		net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
		if (level == null)
			return;
		net.minecraft.world.entity.Entity entity = level.getEntity(payload.entityId());
		if (entity instanceof VillagiumMob<?> mob)
			mob.setData(ModAttachments.PROFILE_DATA.get(), payload.profileData().withEntity(mob));
	}
	
	public static void handleOpenInteractionScreen(MenuDataPacket payload) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;
		
		Minecraft.getInstance().execute(() -> {
			if (level.getEntity(payload.entityId()) instanceof VillagiumMob<?> me) {
				NpcMenuScreen screen = NpcMenuRegistry.createScreen(
						(EntityType<? extends VillagiumMob<?>>) me.getType(), payload);
				if (screen != null) {
					VillagiumClient.activeInteractionMob = me;
					Minecraft.getInstance().setScreenAndShow(screen);
				}
			}
		});
	}
	
	public static void handleOpenInteractionScreen(S2C_OpenInteractMenuPacket payload) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;
		
		Minecraft.getInstance().execute(() -> {
			//			if (level.getEntity(payload.entityId()) instanceof VillagiumMob<?> me) {
			//				InteractionScreen screen = NpcMenuRegistry.createScreen(
			//						(EntityType<? extends VillagiumMob<?>>) me.getType(), payload);
			//				if (screen != null) {
			//					VillagiumClient.activeInteractionMob = me;
			//					Minecraft.getInstance().setScreenAndShow(screen);
			//				}
			//			}
		});
	}
	
	public static void handleUpdateConversation(S2C_UpdateConversationPacket payload) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;
		
		Minecraft.getInstance().execute(() -> {
			Screen current = Minecraft.getInstance().gui.screen();
			if (current instanceof ConversationScreen screen)
				screen.updateData(payload);
			else {
				ConversationScreen screen = new ConversationScreen(payload);
				Minecraft.getInstance().setScreenAndShow(screen);
			}
		});
	}
}
