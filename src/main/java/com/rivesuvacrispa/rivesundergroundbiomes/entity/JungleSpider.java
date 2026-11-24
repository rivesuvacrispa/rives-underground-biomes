package com.rivesuvacrispa.rivesundergroundbiomes.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public class JungleSpider extends Spider {
    private static final int NAUSEA_TICKS = 8 * 20;

    public JungleSpider(EntityType<? extends JungleSpider> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS), this);
        }
        return hit;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.SPIDER.getDefaultLootTable();
    }
}
