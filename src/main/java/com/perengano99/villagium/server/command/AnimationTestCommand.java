package com.perengano99.villagium.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.animation.AnimationCategory;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.network.NetworkManager;
import com.perengano99.villagium.network.packets.SyncMobAnimationPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Villagium.MODID)
public class AnimationTestCommand {
	
	private static final SimpleCommandExceptionType ERROR_NOT_VILLAGIUM_MOB = new SimpleCommandExceptionType(
			Component.literal("Selected entity is not a VillagiumMob.")
	);
	
	private static final SimpleCommandExceptionType ERROR_ENTITY_NOT_FOUND = new SimpleCommandExceptionType(
			Component.literal("Entity not found in the world.")
	);
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		register(event.getDispatcher());
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nvdebug_animate")
						.requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.MODERATORS)))
						.then(Commands.argument("target_id", StringArgumentType.string())
								.suggests((context, builder) -> {
									CommandSourceStack source = context.getSource();
									Vec3 pos = source.getPosition();
									ServerLevel level = source.getLevel();
									double r = 50.0;
									AABB box = new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
									
									// Suggest nearby VillagiumMobs
									List<? extends VillagiumMob> mobs = level.getEntitiesOfClass(VillagiumMob.class, box);
									for (VillagiumMob<?> mob : mobs)
										builder.suggest(mob.getCuid());
									return builder.buildFuture();
								})
								.then(Commands.literal("start")
										// Specific branch for MOVEMENT to support optional speedFactor
										.then(Commands.literal("movement")
												.then(Commands.argument("animation_id", StringArgumentType.word())
														.suggests((context, builder) -> SharedSuggestionProvider.suggest(
																com.perengano99.villagium.network.SharedAnimationData.getIdsForCategory("movement"),
																builder
														                                                                ))
														.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"), null, "infinite",
																"normal"))
														.then(Commands.argument("loop", BoolArgumentType.bool())
																.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"),
																		BoolArgumentType.getBool(context, "loop"), "infinite", "normal"))
																.then(Commands.argument("duration", StringArgumentType.word())
																		.suggests((context, builder) -> SharedSuggestionProvider.suggest(
																				new String[] { "infinite", "5", "10", "20", "30" },
																				builder
																		                                                                ))
																		.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"),
																				BoolArgumentType.getBool(context, "loop"), StringArgumentType.getString(context, "duration"),
																				"normal"))
																		.then(Commands.argument("speedFactor", StringArgumentType.word())
																				.suggests((context, builder) -> SharedSuggestionProvider.suggest(
																						new String[] { "slow", "normal", "fast" },
																						builder
																				                                                                ))
																				.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"),
																						BoolArgumentType.getBool(context, "loop"),
																						StringArgumentType.getString(context, "duration"),
																						StringArgumentType.getString(context, "speedFactor")))
																		     )
																     )
														     )
												     )
										     )
										// Generic branch for other categories
										.then(Commands.argument("category", StringArgumentType.word())
												.suggests((context, builder) -> SharedSuggestionProvider.suggest(
														Stream.of(AnimationCategory.values())
																.filter(c -> c != AnimationCategory.MOVEMENT)
																.map(Enum::name)
																.map(s -> s.toLowerCase(Locale.ROOT)),
														builder
												                                                                ))
												.then(Commands.argument("animation_id", StringArgumentType.word())
														.suggests((context, builder) -> {
															String catName = StringArgumentType.getString(context, "category");
															return SharedSuggestionProvider.suggest(
																	com.perengano99.villagium.network.SharedAnimationData.getIdsForCategory(catName), builder);
														})
														.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"), null, "infinite",
																"normal"))
														.then(Commands.argument("loop", BoolArgumentType.bool())
																.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"),
																		BoolArgumentType.getBool(context, "loop"), "infinite", "normal"))
																.then(Commands.argument("duration", StringArgumentType.word())
																		.suggests((context, builder) -> SharedSuggestionProvider.suggest(
																				new String[] { "infinite", "5", "10", "20", "30" },
																				builder
																		                                                                ))
																		.executes(context -> startAnimation(context, StringArgumentType.getString(context, "animation_id"),
																				BoolArgumentType.getBool(context, "loop"), StringArgumentType.getString(context, "duration"),
																				"normal"))
																     )
														     )
												     )
										     )
								     )
								.then(Commands.literal("stop")
										.executes(context -> stopAnimation(context, null))
										.then(Commands.argument("animation_id", StringArgumentType.word())
												.suggests((context, builder) -> SharedSuggestionProvider.suggest(com.perengano99.villagium.network.SharedAnimationData.getAllIds(),
														builder))
												.executes(context -> stopAnimation(context, StringArgumentType.getString(context, "animation_id")))
										     )
								     )
						     )
		                   );
	}
	
	private static int startAnimation(CommandContext<CommandSourceStack> context, String animId, Boolean loop, String duration, String speedFactor) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		String targetId = StringArgumentType.getString(context, "target_id");
		
		Vec3 pos = source.getPosition();
		ServerLevel level = source.getLevel();
		double r = 100.0;
		AABB box = new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
		List<? extends VillagiumMob> mobs = level.getEntitiesOfClass(VillagiumMob.class, box);
		
		VillagiumMob<?> mob = null;
		for (VillagiumMob<?> m : mobs)
			if (m.getCuid().equalsIgnoreCase(targetId)) {
				mob = m;
				break;
			}
		
		if (mob == null)
			throw ERROR_ENTITY_NOT_FOUND.create();
		
		byte loopMode = 2; // use default
		if (loop != null)
			loopMode = (byte) (loop ? 1 : 0);
		
		int durationTicks = -1;
		if (duration != null && !duration.equalsIgnoreCase("infinite")) {
			try {
				durationTicks = Integer.parseInt(duration) * 20;
			} catch (NumberFormatException e) {
				// Keep it -1 / infinite
			}
		}
		
		float speedVal = 1.0f; // Default for other categories
		
		String catName = "";
		try {
			catName = StringArgumentType.getString(context, "category");
		} catch (IllegalArgumentException e) {
			catName = "movement"; // Literal branch doesn't have the category argument
		}
		
		if (catName.equalsIgnoreCase("movement")) {
			speedVal = 0.5f; // default "normal" for movement
			if (speedFactor != null) {
				if (speedFactor.equalsIgnoreCase("slow"))
					speedVal = 0.25f;
				else if (speedFactor.equalsIgnoreCase("normal"))
					speedVal = 0.55f;
				else if (speedFactor.equalsIgnoreCase("fast"))
					speedVal = 0.92f;
				else {
					try {
						speedVal = Float.parseFloat(speedFactor);
					} catch (NumberFormatException ex) {
					}
				}
			}
		}
		
		// Freeze AI
		if (!mob.isNoAi()) {
			mob.setNoAi(true);
			mob.addTag("animation_debug_frozen");
		}
		if (durationTicks > 0)
			mob.setManualAnimDuration(durationTicks);
		else
			mob.setManualAnimDuration(-1);
		
		final VillagiumMob<?> finalMob = mob;
		// Send SyncMobAnimationPacket to tracking clients
		NetworkManager.PIPELINE.sendToTracking(finalMob, new SyncMobAnimationPacket(finalMob.getId(), true, animId, loopMode, durationTicks, speedVal));
		
		final float finalSpeedVal = speedVal;
		source.sendSuccess(() -> Component.literal("Sent manual animation '" + animId + "' with speed " + finalSpeedVal + " to " + finalMob.getCuid()), true);
		return 1;
	}
	
	private static int stopAnimation(CommandContext<CommandSourceStack> context, String animId) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		String targetId = StringArgumentType.getString(context, "target_id");
		
		Vec3 pos = source.getPosition();
		ServerLevel level = source.getLevel();
		double r = 100.0;
		AABB box = new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
		List<? extends VillagiumMob> mobs = level.getEntitiesOfClass(VillagiumMob.class, box);
		
		VillagiumMob<?> mob = null;
		for (VillagiumMob<?> m : mobs)
			if (m.getCuid().equalsIgnoreCase(targetId)) {
				mob = m;
				break;
			}
		
		if (mob == null)
			throw ERROR_ENTITY_NOT_FOUND.create();
		
		// Unfreeze AI
		if (mob.entityTags().contains("animation_debug_frozen")) {
			mob.setNoAi(false);
			mob.removeTag("animation_debug_frozen");
		}
		mob.setManualAnimDuration(-1);
		
		final VillagiumMob<?> finalMob = mob;
		// Send SyncMobAnimationPacket to tracking clients with start = false
		NetworkManager.PIPELINE.sendToTracking(finalMob, new SyncMobAnimationPacket(finalMob.getId(), false, animId != null ? animId : "", (byte) 2, -1, 1.0f));
		
		source.sendSuccess(() -> Component.literal("Stopped manual animation for " + finalMob.getCuid()), true);
		return 1;
	}
}
