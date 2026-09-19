package net.umittadelen.portallinkinghelper.integration;

import net.umittadelen.portallinkinghelper.portal.PortalLink;

public interface PortalIntegration {

    void add(String name, PortalLink link);

    boolean remove(String name);

    void show(PortalLink link);

    boolean supportsWaypoints();
}