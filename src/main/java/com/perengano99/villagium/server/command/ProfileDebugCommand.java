package com.perengano99.villagium.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.profile.NvProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Villagium.MODID)
public class ProfileDebugCommand {

	private static final SimpleCommandExceptionType ERROR_ENTITY_NOT_FOUND = new SimpleCommandExceptionType(
			Component.literal("Entity not found in the world.")
	);

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		register(event.getDispatcher());
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nvdebug_profile")
						.requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.MODERATORS)))
						.then(Commands.argument("target_id", StringArgumentType.string())
								.suggests((context, builder) -> {
									CommandSourceStack source = context.getSource();
									Vec3 pos = source.getPosition();
									ServerLevel level = source.getLevel();
									double r = 50.0;
									AABB box = new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
									List<? extends VillagiumMob> mobs = level.getEntitiesOfClass(VillagiumMob.class, box);
									for (VillagiumMob<?> mob : mobs)
										builder.suggest(mob.getCuid());
									return builder.buildFuture();
								})
								.executes(ProfileDebugCommand::printProfile)
						)
		);
	}

	private static int printProfile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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

		NvProfile profile = mob.getOrCreateProfile();
		
		final VillagiumMob<?> finalMob = mob;
		source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(() -> Component.literal("PERFIL: " + finalMob.getCuid()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
		source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(() -> Component.literal("Nombre: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(profile.getDisplayName().getString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("Género: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(profile.getGender().name()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("Cultura: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(profile.getCulture().id().toString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("Personalidad: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(profile.getPersonality().id.toString()).withStyle(ChatFormatting.WHITE)), false);
		
		String traitsStr = profile.getTraits().stream()
				.map(t -> t.id().getPath())
				.collect(Collectors.joining(", "));
		source.sendSuccess(() -> Component.literal("Traits: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(traitsStr.isEmpty() ? "Ninguno" : traitsStr).withStyle(ChatFormatting.WHITE)), false);
				
		source.sendSuccess(() -> Component.literal("Apariencia:").withStyle(ChatFormatting.GRAY), false);
		com.perengano99.villagium.social.profile.AppearanceData app = profile.getData().appearance();
		source.sendSuccess(() -> Component.literal("  - Skin: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(app.skinId().toString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  - Clothes: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(app.clothesId().toString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  - Hair: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(app.hairId().toString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  - Face: ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(app.faceId().toString()).withStyle(ChatFormatting.WHITE)), false);
		source.sendSuccess(() -> Component.literal("  - Tone: Group ").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(app.toneGroupId().toString()).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(", Index ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal(String.valueOf(app.toneIndex())).withStyle(ChatFormatting.WHITE)), false);
				
		source.sendSuccess(() -> Component.literal("========================================").withStyle(ChatFormatting.GOLD), false);

		return 1;
	}
}
