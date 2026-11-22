package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class QuicksandBlock extends PowderSnowBlock {
    private static final VoxelShape FALLING_COLLISION_SHAPE = Shapes.box(0, 0, 0, 1, 0.9, 1);

    public QuicksandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getFeetBlockState().is(this)) {
            entity.makeStuckInBlock(state, new Vec3(0.9, 1.5, 0.9));
        }
        if (level.isClientSide) {
            if (level.random.nextInt(4) == 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        entity.getX(),
                        pos.getY() + 1,
                        entity.getZ(),
                        0,
                        0.1,
                        0);
            }
            return;
        }
        if (entity instanceof LivingEntity
                && level.getBlockState(BlockPos.containing(entity.getEyePosition()))
                        .is(this)) {
            entity.hurt(level.damageSources().inWall(), 1.0F);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
            Entity entity = entityContext.getEntity();
            if (entity.fallDistance > 2.5F) return FALLING_COLLISION_SHAPE;
            if (entity instanceof FallingBlockEntity) return Shapes.block();
        }
        return Shapes.empty();
    }

    // powder snow has an empty occlusion shape which shaded quicksand dark
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        if (!level.isClientSide()) level.levelEvent(2001, pos, Block.getId(state));
        return new ItemStack(ModBlocks.QUICKSAND_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.SAND_BREAK);
    }
}
