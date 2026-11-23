package com.rivesuvacrispa.rivesundergroundbiomes.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.worldgen.IExtendedParameterList;

final class TerraBlenderBiomes {
    @SuppressWarnings("unchecked")
    static Holder<Biome> findValue(
            Climate.ParameterList<Holder<Biome>> parameters, Climate.TargetPoint target, int x, int y, int z) {
        return ((IExtendedParameterList<Holder<Biome>>) parameters).findValuePositional(target, x, y, z);
    }

    private TerraBlenderBiomes() {}
}
