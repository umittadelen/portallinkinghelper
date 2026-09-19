package net.umittadelen.portallinkinghelper;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

import net.umittadelen.portallinkinghelper.command.PortalCommand;
import net.umittadelen.portallinkinghelper.integration.IntegrationManager;
import net.umittadelen.portallinkinghelper.integration.PortalIntegration;

public final class PortalLinkingHelper implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Minecraft client = Minecraft.getInstance();

        IntegrationManager integrationManager =
                new IntegrationManager(client);

        PortalIntegration integration =
                integrationManager.getIntegration();

        PortalCommand.register(integration);
    }
}