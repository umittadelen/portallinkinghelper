package net.umittadelen.portallinkinghelper.integration.none;

import net.minecraft.client.Minecraft;

import net.umittadelen.portallinkinghelper.integration.PortalIntegration;
import net.umittadelen.portallinkinghelper.message.PortalMessage;
import net.umittadelen.portallinkinghelper.portal.PortalLink;

public final class CoordinateIntegration implements PortalIntegration {

    private final Minecraft client;

    public CoordinateIntegration(Minecraft client) {
        this.client = client;
    }

    @Override
    public void add(String name, PortalLink link) {
        // No waypoint system is available.
        // Just display the calculated coordinates.
        show(link);
    }

    @Override
    public boolean remove(String name) {
        // There is nothing stored to remove.
        return false;
    }

    @Override
    public boolean supportsWaypoints() {
        return false;
    }

    @Override
    public void show(PortalLink link) {
        PortalMessage.coordinates(client, link);
    }
}