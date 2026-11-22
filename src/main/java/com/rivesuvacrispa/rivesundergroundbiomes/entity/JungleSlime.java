package com.rivesuvacrispa.rivesundergroundbiomes.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class JungleSlime extends Slime {
    private static final int THORNS_LEVEL = 3;

    public JungleSlime(EntityType<? extends JungleSlime> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt
                && source.getDirectEntity() instanceof LivingEntity attacker
                && !source.is(DamageTypeTags.AVOIDS_GUARDIAN_THORNS)
                && ThornsEnchantment.shouldHit(THORNS_LEVEL, random)) {
            attacker.hurt(damageSources().thorns(this), ThornsEnchantment.getDamage(THORNS_LEVEL, random));
        }
        return hurt;
    }

    // the vanilla slime loot table checks the size itself so it works for any Slime subclass
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.SLIME.getDefaultLootTable();
    }

    public static boolean checkJungleSlimeSpawnRules(
            EntityType<JungleSlime> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }
}
