package com.rivesuvacrispa.rivesundergroundbiomes.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class GlowshroomItem extends BlockItem implements SignApplicator {
    public GlowshroomItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity sign, boolean front, Player player) {
        if (!sign.updateText(text -> text.setHasGlowingText(true), front)) return false;
        level.playSound(null, sign.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
