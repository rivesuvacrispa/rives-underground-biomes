package com.rivesuvacrispa.rivesundergroundbiomes.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

public class MummySkeleton extends Skeleton {
    public MummySkeleton(EntityType<? extends MummySkeleton> type, Level level) {
        super(type, level);
    }
}
