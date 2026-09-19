package net.umittadelen.portallinkinghelper.integration;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import net.umittadelen.portallinkinghelper.integration.none.CoordinateIntegration;

public final class IntegrationManager {

    private static final String XAERO_MOD_ID = "xaerominimap";

    private static final String XAERO_INTEGRATION_CLASS =
            "net.umittadelen.portallinkinghelper.integration.xaero.XaeroIntegration";

    private final PortalIntegration integration;

    public IntegrationManager(Minecraft client) {
        this.integration = createIntegration(client);
    }

    public PortalIntegration getIntegration() {
        return integration;
    }

    private static PortalIntegration createIntegration(
            Minecraft client
    ) {
        if (!FabricLoader.getInstance().isModLoaded(XAERO_MOD_ID)) {
            return new CoordinateIntegration(client);
        }

        try {
            Class<?> integrationClass =
                    Class.forName(XAERO_INTEGRATION_CLASS);

            Object instance =
                    integrationClass
                            .getConstructor(Minecraft.class)
                            .newInstance(client);

            if (!(instance instanceof PortalIntegration portalIntegration)) {
                throw new IllegalStateException(
                        "XaeroIntegration does not implement PortalIntegration"
                );
            }

            return portalIntegration;

        } catch (Exception e) {
            System.err.println(
                    "[Portal Linking Helper] "
                            + "Failed to load Xaero integration: "
                            + e
            );

            return new CoordinateIntegration(client);
        }
    }
}
