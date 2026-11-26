package com.rivesuvacrispa.rivesundergroundbiomes.world;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

record ReefTube(
        double x,
        double z,
        double bendX,
        double bendZ,
        double top,
        double bottom,
        double radius,
        BlockState wall,
        @Nullable BlockState vent) {
    private static final double WALL = 1.5;

    boolean reaches(int bx, int bz) {
        double reach = Math.max(Math.abs(bendX), Math.abs(bendZ)) + radius + WALL;
        return Math.abs(bx + 0.5 - x) <= reach && Math.abs(bz + 0.5 - z) <= reach;
    }

    boolean inside(int bx, int y, int bz) {
        double cy = y + 0.5;
        if (cy > top || cy < bottom) return false;
        double bend = Math.sin(Math.PI * (top - cy) / (top - bottom));
        double dx = bx + 0.5 - (x + bendX * bend);
        double dz = bz + 0.5 - (z + bendZ * bend);
        return dx * dx + dz * dz <= radius * radius;
    }

    boolean isWall(int bx, int y, int bz) {
        return !inside(bx, y, bz)
                && (inside(bx + 1, y, bz)
                        || inside(bx - 1, y, bz)
                        || inside(bx, y, bz + 1)
                        || inside(bx, y, bz - 1)
                        || inside(bx, y + 1, bz)
                        || inside(bx, y - 1, bz));
    }
}
