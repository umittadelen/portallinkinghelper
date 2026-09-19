package net.umittadelen.portallinkinghelper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.umittadelen.portallinkinghelper.integration.PortalIntegration;
import net.umittadelen.portallinkinghelper.portal.PortalCalculator;
import net.umittadelen.portallinkinghelper.portal.PortalLink;

public final class PortalCommand {

    private PortalCommand() {
    }

    public static void register(
            PortalIntegration integration
    ) {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {

                    if (integration.supportsWaypoints()) {
                        registerWaypointCommands(
                                dispatcher,
                                integration
                        );
                    } else {
                        registerCoordinateCommand(
                                dispatcher,
                                integration
                        );
                    }
                }
        );
    }

    private static void registerWaypointCommands(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            PortalIntegration integration
    ) {
        dispatcher.register(
                ClientCommandManager.literal("locateportal")

                        .then(
                                ClientCommandManager.literal("add")

                                        .then(
                                                ClientCommandManager.argument(
                                                                "name",
                                                                StringArgumentType.string()
                                                        )

                                                        .executes(context -> {

                                                            String name =
                                                                    StringArgumentType.getString(
                                                                            context,
                                                                            "name"
                                                                    );

                                                            Vec3 position =
                                                                    context.getSource()
                                                                            .getPlayer()
                                                                            .position();

                                                            return add(
                                                                    integration,
                                                                    name,
                                                                    position
                                                            );
                                                        })

                                                        .then(
                                                                ClientCommandManager.argument(
                                                                                "pos",
                                                                                Vec3Argument.vec3()
                                                                        )

                                                                        .executes(context -> {

                                                                            String name =
                                                                                    StringArgumentType.getString(
                                                                                            context,
                                                                                            "name"
                                                                                    );

                                                                            Coordinates coordinates =
                                                                                    context.getArgument(
                                                                                            "pos",
                                                                                            Coordinates.class
                                                                                    );

                                                                            Vec3 position =
                                                                                    coordinates.getPosition(
                                                                                            context.getSource()
                                                                                                    .getPlayer()
                                                                                                    .createCommandSourceStack()
                                                                                    );

                                                                            return add(
                                                                                    integration,
                                                                                    name,
                                                                                    position
                                                                            );
                                                                        })
                                                        )
                                        )
                        )

                        .then(
                                ClientCommandManager.literal("remove")

                                        .then(
                                                ClientCommandManager.argument(
                                                                "name",
                                                                StringArgumentType.string()
                                                        )

                                                        .executes(context -> {

                                                            String name =
                                                                    StringArgumentType.getString(
                                                                            context,
                                                                            "name"
                                                                    );

                                                            integration.remove(name);

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    private static void registerCoordinateCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            PortalIntegration integration
    ) {
        dispatcher.register(
                ClientCommandManager.literal("locateportal")

                        .executes(context -> {

                            Vec3 position =
                                    context.getSource()
                                            .getPlayer()
                                            .position();

                            return showCoordinates(
                                    integration,
                                    position
                            );
                        })

                        .then(
                                ClientCommandManager.argument(
                                                "pos",
                                                Vec3Argument.vec3()
                                        )

                                        .executes(context -> {

                                            Coordinates coordinates =
                                                    context.getArgument(
                                                            "pos",
                                                            Coordinates.class
                                                    );

                                            Vec3 position =
                                                    coordinates.getPosition(
                                                            context.getSource()
                                                                    .getPlayer()
                                                                    .createCommandSourceStack()
                                                    );

                                            return showCoordinates(
                                                    integration,
                                                    position
                                            );
                                        })
                        )
        );
    }

    private static int add(
            PortalIntegration integration,
            String name,
            Vec3 position
    ) {
        Minecraft client =
                Minecraft.getInstance();

        if (!isValidDimension(client)) {
            return 0;
        }

        PortalLink link =
                calculate(client, position);

        integration.add(name, link);

        return 1;
    }

    private static int showCoordinates(
            PortalIntegration integration,
            Vec3 position
    ) {
        Minecraft client =
                Minecraft.getInstance();

        if (!isValidDimension(client)) {
            return 0;
        }

        PortalLink link =
                calculate(client, position);

        integration.show(link);

        return 1;
    }

    private static PortalLink calculate(
            Minecraft client,
            Vec3 position
    ) {
        return PortalCalculator.calculate(
                client.level.dimension(),
                position
        );
    }

    private static boolean isValidDimension(
            Minecraft client
    ) {
        if (client.level == null ||
                client.player == null) {

            return false;
        }

        boolean overworld =
                client.level.dimension()
                        == Level.OVERWORLD;

        boolean nether =
                client.level.dimension()
                        == Level.NETHER;

        if (!overworld && !nether) {
            client.player.displayClientMessage(
                    Component.literal(
                            "§clocateportal only works in the Overworld or Nether"
                    ),
                    false
            );

            return false;
        }

        return true;
    }
}