package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.Ant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiPredicate;

public class AnthillBlock extends SmallPlantBlock {
    public AnthillBlock(Properties properties, VoxelShape shape, BiPredicate<BlockState, BlockGetter> ground) {
        super(properties, shape, ground);
    }

    // ants come out only here and in entityInside, not in spawnAfterBreak
    // so anthills washed away by water or blown up release none
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (level instanceof ServerLevel server
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem()) == 0) {
            releaseAnts(server, pos, player);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof Player player && !player.isSpectator()) {
            level.destroyBlock(pos, false, player);
            releaseAnts(server, pos, player);
        }
    }

    private static void releaseAnts(ServerLevel level, BlockPos pos, Player player) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;
        int count = 1 + level.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            Ant ant = ModEntities.ANT.create(level);
            if (ant == null) return;
            ant.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            if (!player.getAbilities().instabuild) ant.setTarget(player);
            level.addFreshEntity(ant);
            ant.spawnAnim();
        }
    }
}
