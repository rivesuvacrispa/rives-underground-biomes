package com.rivesuvacrispa.rivesundergroundbiomes.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class Frozen extends Zombie {
    private static final int FREEZE_TICKS = 60;

    public Frozen(EntityType<? extends Frozen> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living && living.canFreeze()) {
            // outside powder snow frozen ticks thaw by 2 per tick
            // so the target stays fully frozen for FREEZE_TICKS
            living.setTicksFrozen(
                    Math.max(living.getTicksFrozen(), living.getTicksRequiredToFreeze() + FREEZE_TICKS * 2));
        }
        return hit;
    }

    @Override
    protected void doUnderWaterConversion() {
        convertToZombieType(EntityType.ZOMBIE);
        if (!isSilent()) level().levelEvent(null, 1041, blockPosition(), 0);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.ZOMBIE.getDefaultLootTable();
    }
}
