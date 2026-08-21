package net.umittadelen.xaerosportallocater;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
					.then(ClientCommandManager.argument("pos", Vec3Argument.vec3())
						.executes(ctx -> {
							Coordinates coords = ctx.getArgument("pos", Coordinates.class);
							Vec3 pos = coords.getPosition(ctx.getSource().getPlayer().createCommandSourceStack());
							return run(pos);
						}))
			)
		);
	}

	private static int run(Vec3 pos) {
		Minecraft client = Minecraft.getInstance();

		if (client.level == null || client.player == null) {
			return 0;
		}

		boolean inNether = client.level.dimension() == Level.NETHER;

		Vec3 overworldPos;
		Vec3 netherPos;

		if (inNether) {
			netherPos = pos;
			overworldPos = new Vec3(
					pos.x * 8,
					pos.y,
					pos.z * 8
			);
		} else {
			overworldPos = pos;
			netherPos = new Vec3(
					pos.x * 0.125,
					pos.y,
					pos.z * 0.125
			);
		}

		try {
			// Get Xaero's currently active HUD session.
			HudSession hudSession = HudSession.getCurrentSession();

			if (hudSession == null) {
				throw new RuntimeException("Xaero HUD session is not available");
			}

			// Get Xaero's actual live MinimapSession.
			MinimapSession session =
					hudSession.getSession(BuiltInHudModules.MINIMAP);

			if (session == null) {
				throw new RuntimeException("Xaero Minimap session is not available");
			}

			MinimapWorldManager worldManager =
					session.getWorldManager();

			// Get Xaero's root world container.
			XaeroPath root =
					session.getWorldState()
							.getAutoRootContainerPath();

			// Let Xaero determine the dimension directory names.
			String overworldDimension =
					session.getDimensionHelper()
							.getDimensionDirectoryName(Level.OVERWORLD);

			String netherDimension =
					session.getDimensionHelper()
							.getDimensionDirectoryName(Level.NETHER);

			// Singleplayer world structure:
			// <root>/<dimension>/waypoints
			XaeroPath overworldPath =
					root
							.resolve(overworldDimension)
							.resolve("waypoints");

			XaeroPath netherPath =
					root
							.resolve(netherDimension)
							.resolve("waypoints");

			// Get the two MinimapWorld objects.
			MinimapWorld overworldWorld =
					worldManager.getWorld(overworldPath);

			MinimapWorld netherWorld =
					worldManager.getWorld(netherPath);

			if (overworldWorld == null || netherWorld == null) {
				throw new RuntimeException("Could not get Xaero worlds");
			}

			/*
			 * TODO: Replace this part with an actual good logic
			 * portal-1, portal-2, portal-3, etc.
			 */
			String name = "portal-1"; /* NOTE: Uses a fixed waypoint name for now*/

			// add waypoints for nether and overworld.
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