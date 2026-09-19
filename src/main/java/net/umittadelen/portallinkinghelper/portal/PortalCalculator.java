package net.umittadelen.portallinkinghelper.portal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class PortalCalculator {

    private PortalCalculator() {
    }

    public static PortalLink calculate(
            ResourceKey<Level> dimension,
            Vec3 position
    ) {
        double x = Math.floor(position.x);
        double y = Math.floor(position.y);
        double z = Math.floor(position.z);

        if (dimension == Level.NETHER) {
            return new PortalLink(
                    new Vec3(x * 8, y, z * 8),
                    new Vec3(x, y, z)
            );
        }

        if (dimension == Level.OVERWORLD) {
            return new PortalLink(
                    new Vec3(x, y, z),
                    new Vec3(
                            Math.floor(x / 8),
                            y,
                            Math.floor(z / 8)
                    )
            );
        }

        throw new IllegalArgumentException(
                "Portal linking only works in the Overworld or Nether"
        );
    }
}