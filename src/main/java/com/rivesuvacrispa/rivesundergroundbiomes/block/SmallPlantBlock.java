package com.rivesuvacrispa.rivesundergroundbiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiPredicate;

public class SmallPlantBlock extends BushBlock {
    private final VoxelShape shape;
    private final BiPredicate<BlockState, BlockGetter> ground;

    public SmallPlantBlock(Properties properties, VoxelShape shape, BiPredicate<BlockState, BlockGetter> ground) {
        super(properties);
        this.shape = shape;
        this.ground = ground;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return ground.test(state, level) && state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return shape.move(offset.x, offset.y, offset.z);
    }
}
