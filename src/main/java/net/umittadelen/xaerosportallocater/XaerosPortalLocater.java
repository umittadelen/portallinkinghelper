package net.umittadelen.xaerosportallocater;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.io.IOException;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.path.XaeroPath;
import xaero.hud.HudSession;
import xaero.hud.minimap.BuiltInHudModules;

public class XaerosPortalLocater implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal("locateportal")
					.then(ClientCommandManager.literal("add")
						.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.executes(ctx -> {
								String name = StringArgumentType.getString(ctx, "name");
								Vec3 pos = ctx.getSource().getPlayer().position();
								return run(name, pos);
							})
							.then(ClientCommandManager.argument("pos", Vec3Argument.vec3())
								.executes(ctx -> {
									String name = StringArgumentType.getString(ctx, "name");
									Coordinates coords = ctx.getArgument("pos", Coordinates.class);
									Vec3 pos = coords.getPosition(ctx.getSource().getPlayer().createCommandSourceStack());
									return run(name, pos);
								})
							)
						)
					)
					.then(ClientCommandManager.literal("remove")
						.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.executes(ctx -> {
								String name = StringArgumentType.getString(ctx, "name");
								return remove(name);
							})
						)
					)
			)
		);
	}

	private static int run(String rawName, Vec3 pos) {
		Minecraft client = Minecraft.getInstance();

		if (client.level == null || client.player == null) {
			return 0;
		}

		boolean inNether = client.level.dimension() == Level.NETHER;
		boolean inOverworld = client.level.dimension() == Level.OVERWORLD;

		if (!inNether && !inOverworld) {
			client.player.displayClientMessage(
				Component.literal(
					"§clocateportal only works in the Overworld or Nether"
				),
				false
			);
			return 0;
		}

		// Floor the position in the dimension we're actually standing in FIRST,
		// then derive the other dimension's coordinate from that integer.
		double px = Math.floor(pos.x);
		double py = Math.floor(pos.y);
		double pz = Math.floor(pos.z);

		Vec3 overworldPos;
		Vec3 netherPos;

		if (inNether) {
			netherPos = new Vec3(px, py, pz);
			overworldPos = new Vec3(
				px * 8,
				py,
				pz * 8
			);
		} else {
			overworldPos = new Vec3(px, py, pz);
			netherPos = new Vec3(
				Math.floor(px / 8),
				py,
				Math.floor(pz / 8)
			);
		}

		try {
			MinimapWorld[] worlds = getOverworldAndNetherWorlds();
			MinimapWorld overworldWorld = worlds[0];
			MinimapWorld netherWorld = worlds[1];

			String name = "P_" + rawName;

			// ensure the name is unique in both worlds, appending -2, -3, ... if needed
			name = uniqueName(overworldWorld, netherWorld, name);

			addWaypoint(
				overworldWorld,
				name,
				overworldPos,
				WaypointColor.RED
			);

			addWaypoint(
				netherWorld,
				name,
				netherPos,
				WaypointColor.PURPLE
			);

			saveWorld(overworldWorld);
			saveWorld(netherWorld);

			client.player.displayClientMessage(
				Component.literal(
					"§aCreated 2 linked waypoints: " + name
				),
				false
			);

		} catch (Exception e) {
			client.player.displayClientMessage(
				Component.literal(
					"§clocateportal failed: " + e.getMessage()
				),
				false
			);
		}

		return 1;
	}

	private static MinimapWorld[] getOverworldAndNetherWorlds() {
		HudSession hudSession = HudSession.getCurrentSession();

		if (hudSession == null) {
			throw new RuntimeException("Xaero HUD session is not available");
		}

		MinimapSession session =
			hudSession.getSession(BuiltInHudModules.MINIMAP);

		if (session == null) {
			throw new RuntimeException("Xaero Minimap session is not available");
		}

		MinimapWorldManager worldManager =
			session.getWorldManager();

		XaeroPath root =
			session.getWorldState()
				.getAutoRootContainerPath();

		String overworldDimension =
			session.getDimensionHelper()
				.getDimensionDirectoryName(Level.OVERWORLD);

		String netherDimension =
			session.getDimensionHelper()
				.getDimensionDirectoryName(Level.NETHER);

		XaeroPath overworldPath =
			root
				.resolve(overworldDimension)
				.resolve("waypoints");

		XaeroPath netherPath =
			root
				.resolve(netherDimension)
				.resolve("waypoints");

		MinimapWorld overworldWorld =
			worldManager.getWorld(overworldPath);

		MinimapWorld netherWorld =
			worldManager.getWorld(netherPath);

		if (overworldWorld == null || netherWorld == null) {
			throw new RuntimeException("Could not get Xaero worlds");
		}

		return new MinimapWorld[] { overworldWorld, netherWorld };
	}

	private static void saveWorld(MinimapWorld world) {
		HudSession hudSession = HudSession.getCurrentSession();

		if (hudSession == null) {
			return;
		}

		MinimapSession session =
			hudSession.getSession(BuiltInHudModules.MINIMAP);

		if (session == null) {
			return;
		}

		try {
			session.getWorldManagerIO().saveWorld(world);
		} catch (IOException e) {
			// suppressed, matches Xaero's own DeathpointHandler handling
		}
	}

	private static int remove(String rawName) {
		Minecraft client = Minecraft.getInstance();

		if (client.level == null || client.player == null) {
			return 0;
		}

		String name = "P_" + rawName;

		try {
			MinimapWorld[] worlds = getOverworldAndNetherWorlds();
			MinimapWorld overworldWorld = worlds[0];
			MinimapWorld netherWorld = worlds[1];

			boolean removedOverworld = removeWaypoint(overworldWorld, name);
			boolean removedNether = removeWaypoint(netherWorld, name);

			if (!removedOverworld && !removedNether) {
				client.player.displayClientMessage(
					Component.literal(
					"§cNo waypoints named " + name + " found"
					),
					false
				);
				return 0;
			}

			saveWorld(overworldWorld);
			saveWorld(netherWorld);

			client.player.displayClientMessage(
				Component.literal(
				"§aRemoved waypoints: " + name
				),
				false
			);

		} catch (Exception e) {
			client.player.displayClientMessage(
				Component.literal(
				"§clocateportal remove failed: " + e.getMessage()
				),
				false
			);
		}

		return 1;
	}

	private static boolean removeWaypoint(MinimapWorld world, String name) {
		WaypointSet set = world.getCurrentWaypointSet();

		if (set == null) {
			return false;
		}

		Waypoint toRemove = null;

		for (Waypoint wp : set.getWaypoints()) {
			if (name.equals(wp.getName())) {
				toRemove = wp;
				break;
			}
		}

		if (toRemove == null) {
			return false;
		}

		set.remove(toRemove);
		return true;
	}

	private static String uniqueName(MinimapWorld overworldWorld, MinimapWorld netherWorld, String baseName) {
		String name = baseName;
		int counter = 2;

		while (nameExists(overworldWorld, name) || nameExists(netherWorld, name)) {
			name = baseName + "-" + counter;
			counter++;
		}

		return name;
	}

	private static boolean nameExists(MinimapWorld world, String name) {
		WaypointSet set = world.getCurrentWaypointSet();

		if (set == null) {
			return false;
		}

		for (Waypoint wp : set.getWaypoints()) {
			if (name.equals(wp.getName())) {
				return true;
			}
		}

		return false;
	}

	private static void addWaypoint(
		MinimapWorld world,
		String name,
		Vec3 pos,
		WaypointColor color
	) {
		WaypointSet set = world.getCurrentWaypointSet();

		if (set == null) {
			return;
		}

		Waypoint waypoint = new Waypoint(
			(int) Math.floor(pos.x),
			(int) Math.floor(pos.y),
			(int) Math.floor(pos.z),
			name,
			"P",
			color,
			WaypointPurpose.NORMAL
		);

		set.add(waypoint, true);
	}
}