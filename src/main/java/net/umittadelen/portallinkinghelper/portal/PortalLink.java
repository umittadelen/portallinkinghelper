package net.umittadelen.portallinkinghelper.portal;

import net.minecraft.world.phys.Vec3;

public record PortalLink(
        Vec3 overworld,
        Vec3 nether
) {
}