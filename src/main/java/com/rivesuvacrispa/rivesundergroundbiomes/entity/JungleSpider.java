package com.rivesuvacrispa.rivesundergroundbiomes.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public class JungleSpider extends Spider {
    public JungleSpider(EntityType<? extends JungleSpider> type, Level level) {
        super(type, level);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.SPIDER.getDefaultLootTable();
    }
}
