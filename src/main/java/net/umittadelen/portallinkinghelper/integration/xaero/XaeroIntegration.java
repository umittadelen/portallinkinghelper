package net.umittadelen.portallinkinghelper.integration.xaero;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.umittadelen.portallinkinghelper.integration.PortalIntegration;
import net.umittadelen.portallinkinghelper.message.PortalMessage;
import net.umittadelen.portallinkinghelper.portal.PortalLink;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.HudSession;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.path.XaeroPath;

public final class XaeroIntegration implements PortalIntegration {

    private static final String WAYPOINT_PREFIX = "P_";

    private final Minecraft client;

    public XaeroIntegration(Minecraft client) {
        this.client = client;
    }

    @Override
    public void add(String rawName, PortalLink link) {
        if (client.player == null) {
            return;
        }

        try {
            MinimapWorld[] worlds = getOverworldAndNetherWorlds();

            MinimapWorld overworldWorld = worlds[0];
            MinimapWorld netherWorld = worlds[1];

            String name = WAYPOINT_PREFIX + rawName;
            name = uniqueName(
                    overworldWorld,
                    netherWorld,
                    name
            );

            addWaypoint(
                    overworldWorld,
                    name,
                    link.overworld(),
                    WaypointColor.RED
            );

            addWaypoint(
                    netherWorld,
                    name,
                    link.nether(),
                    WaypointColor.PURPLE
            );

            saveWorld(overworldWorld);
            saveWorld(netherWorld);

            PortalMessage.created(
                    client,
                    name,
                    link
            );

        } catch (Exception e) {
            PortalMessage.error(
                    client,
                    "Could not create Xaero waypoints: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public boolean remove(String rawName) {
        if (client.player == null) {
            return false;
        }

        String name = WAYPOINT_PREFIX + rawName;

        try {
            MinimapWorld[] worlds = getOverworldAndNetherWorlds();

            MinimapWorld overworldWorld = worlds[0];
            MinimapWorld netherWorld = worlds[1];

            boolean removedOverworld =
                    removeWaypoint(
                            overworldWorld,
                            name
                    );

            boolean removedNether =
                    removeWaypoint(
                            netherWorld,
                            name
                    );

            if (!removedOverworld && !removedNether) {
                PortalMessage.notFound(
                        client,
                        name
                );

                return false;
            }

            saveWorld(overworldWorld);
            saveWorld(netherWorld);

            PortalMessage.removed(
                    client,
                    name
            );

            return true;

        } catch (Exception e) {
            PortalMessage.error(
                    client,
                    "Could not remove Xaero waypoints: "
                            + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean supportsWaypoints() {
        return true;
    }

    @Override
    public void show(PortalLink link) {
        PortalMessage.coordinates(
                client,
                link
        );
    }

    private MinimapWorld[] getOverworldAndNetherWorlds() {

        HudSession hudSession =
                HudSession.getCurrentSession();

        if (hudSession == null) {
            throw new IllegalStateException(
                    "Xaero HUD session is not available"
            );
        }

        MinimapSession session =
                hudSession.getSession(
                        BuiltInHudModules.MINIMAP
                );

        if (session == null) {
            throw new IllegalStateException(
                    "Xaero Minimap session is not available"
            );
        }

        MinimapWorldManager worldManager =
                session.getWorldManager();

        XaeroPath root =
                session.getWorldState()
                        .getAutoRootContainerPath();

        String overworldDimension =
                session.getDimensionHelper()
                        .getDimensionDirectoryName(
                                Level.OVERWORLD
                        );

        String netherDimension =
                session.getDimensionHelper()
                        .getDimensionDirectoryName(
                                Level.NETHER
                        );

        XaeroPath overworldPath =
                root.resolve(overworldDimension)
                        .resolve("waypoints");

        XaeroPath netherPath =
                root.resolve(netherDimension)
                        .resolve("waypoints");

        MinimapWorld overworldWorld =
                worldManager.getWorld(
                        overworldPath
                );

        MinimapWorld netherWorld =
                worldManager.getWorld(
                        netherPath
                );

        if (overworldWorld == null ||
                netherWorld == null) {

            throw new IllegalStateException(
                    "Could not get Xaero worlds"
            );
        }

        return new MinimapWorld[] {
                overworldWorld,
                netherWorld
        };
    }

    private void saveWorld(MinimapWorld world) {

        HudSession hudSession =
                HudSession.getCurrentSession();

        if (hudSession == null) {
            return;
        }

        MinimapSession session =
                hudSession.getSession(
                        BuiltInHudModules.MINIMAP
                );

        if (session == null) {
            return;
        }

        try {
            session.getWorldManagerIO()
                    .saveWorld(world);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not save Xaero world",
                    e
            );
        }
    }

    private boolean removeWaypoint(
            MinimapWorld world,
            String name
    ) {
        WaypointSet set =
                world.getCurrentWaypointSet();

        if (set == null) {
            return false;
        }

        Waypoint toRemove = null;

        for (Waypoint waypoint : set.getWaypoints()) {
            if (name.equals(waypoint.getName())) {
                toRemove = waypoint;
                break;
            }
        }

        if (toRemove == null) {
            return false;
        }

        set.remove(toRemove);
        return true;
    }

    private String uniqueName(
            MinimapWorld overworldWorld,
            MinimapWorld netherWorld,
            String baseName
    ) {
        String name = baseName;
        int counter = 2;

        while (
                nameExists(overworldWorld, name) ||
                        nameExists(netherWorld, name)
        ) {
            name = baseName + "-" + counter;
            counter++;
        }

        return name;
    }

    private boolean nameExists(
            MinimapWorld world,
            String name
    ) {
        WaypointSet set =
                world.getCurrentWaypointSet();

        if (set == null) {
            return false;
        }

        for (Waypoint waypoint : set.getWaypoints()) {
            if (name.equals(waypoint.getName())) {
                return true;
            }
        }

        return false;
    }

    private void addWaypoint(
            MinimapWorld world,
            String name,
            Vec3 position,
            WaypointColor color
    ) {
        WaypointSet set =
                world.getCurrentWaypointSet();

        if (set == null) {
            throw new IllegalStateException(
                    "Xaero waypoint set is not available"
            );
        }

        Waypoint waypoint =
                new Waypoint(
                        (int) Math.floor(position.x),
                        (int) Math.floor(position.y),
                        (int) Math.floor(position.z),
                        name,
                        "P",
                        color,
                        WaypointPurpose.NORMAL
                );

        set.add(
                waypoint,
                true
        );
    }
}