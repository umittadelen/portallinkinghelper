package net.umittadelen.portallinkinghelper.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import net.umittadelen.portallinkinghelper.portal.PortalLink;

public final class PortalMessage {

    private PortalMessage() {
    }

    public static void coordinates(
            Minecraft client,
            PortalLink link
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§aPortal coordinates:\n" +
                                "§fOverworld: §b" +
                                format(link.overworld()) +
                                "\n" +
                                "§fNether: §5" +
                                format(link.nether())
                ),
                false
        );
    }

    public static void created(
            Minecraft client,
            String name,
            PortalLink link
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§aPortal link created: §f" +
                                name +
                                "\n" +
                                "§fOverworld: §b" +
                                format(link.overworld()) +
                                "\n" +
                                "§fNether: §5" +
                                format(link.nether())
                ),
                false
        );
    }

    public static void added(
            Minecraft client,
            String name
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§aPortal link created: §f" + name
                ),
                false
        );
    }

    public static void removed(
            Minecraft client,
            String name
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§aPortal link removed: §f" + name
                ),
                false
        );
    }

    public static void notFound(
            Minecraft client,
            String name
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§cNo portal link named §f" +
                                name +
                                " §cfound"
                ),
                false
        );
    }

    public static void error(
            Minecraft client,
            String message
    ) {
        if (client.player == null) {
            return;
        }

        client.player.displayClientMessage(
                Component.literal(
                        "§cPortal Linking Helper: §f" +
                                message
                ),
                false
        );
    }

    private static String format(Vec3 pos) {
        return (int) Math.floor(pos.x)
                + ", "
                + (int) Math.floor(pos.y)
                + ", "
                + (int) Math.floor(pos.z);
    }
}