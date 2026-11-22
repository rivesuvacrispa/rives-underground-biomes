package com.rivesuvacrispa.rivesundergroundbiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// vanilla dripstone logic only recognizes pointed dripstone itself
// so this is a lean copy without growth
public class SpeleothemBlock extends Block {
    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;

    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5, 0, 5, 11, 16, 11);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5, 0, 5, 11, 11, 11);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5, 5, 5, 11, 16, 11);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape BASE_SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    // null = no drip particles
    @Nullable
    private final ParticleOptions drip;

    public SpeleothemBlock(Properties properties, @Nullable ParticleOptions drip) {
        super(properties);
        this.drip = drip;
        registerDefaultState(stateDefinition
                .any()
                .setValue(TIP_DIRECTION, Direction.DOWN)
                .setValue(THICKNESS, DripstoneThickness.TIP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION, THICKNESS);
    }

    // index 0 touches the support
    public BlockState columnState(Direction tip, int index, int length) {
        DripstoneThickness thickness;
        if (index == length - 1) thickness = DripstoneThickness.TIP;
        else if (index == length - 2) thickness = DripstoneThickness.FRUSTUM;
        else if (index == 0) thickness = DripstoneThickness.BASE;
        else thickness = DripstoneThickness.MIDDLE;
        return defaultBlockState().setValue(TIP_DIRECTION, tip).setValue(THICKNESS, thickness);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction looking = context.getNearestLookingVerticalDirection().getOpposite();
        Direction tip = canAttach(level, pos, looking)
                ? looking
                : canAttach(level, pos, looking.getOpposite()) ? looking.getOpposite() : null;
        if (tip == null) return null;
        return defaultBlockState().setValue(TIP_DIRECTION, tip).setValue(THICKNESS, thickness(level, pos, tip));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canAttach(level, pos, state.getValue(TIP_DIRECTION));
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction.getAxis() != Direction.Axis.Y) return state;
        Direction tip = state.getValue(TIP_DIRECTION);
        if (!canAttach(level, pos, tip)) {
            level.scheduleTick(pos, this, tip == Direction.DOWN ? 2 : 1);
            return state;
        }
        return state.setValue(THICKNESS, thickness(level, pos, tip));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (canSurvive(state, level, pos)) return;
        if (state.getValue(TIP_DIRECTION) == Direction.DOWN) {
            FallingBlockEntity.fall(level, pos, state).setHurtsEntities(2.0F, 30);
        } else {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP && state.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(
                    fallDistance + 2.0F, 2.0F, level.damageSources().stalagmite());
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (drip == null
                || state.getValue(TIP_DIRECTION) != Direction.DOWN
                || state.getValue(THICKNESS) != DripstoneThickness.TIP) return;
        if (random.nextFloat() >= 0.03F) return;
        Vec3 offset = state.getOffset(level, pos);
        level.addParticle(drip, pos.getX() + 0.5 + offset.x, pos.getY() + 0.3, pos.getZ() + 0.5 + offset.z, 0, 0, 0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = switch (state.getValue(THICKNESS)) {
            case TIP_MERGE -> TIP_MERGE_SHAPE;
            case TIP -> state.getValue(TIP_DIRECTION) == Direction.DOWN ? TIP_SHAPE_DOWN : TIP_SHAPE_UP;
            case FRUSTUM -> FRUSTUM_SHAPE;
            case MIDDLE -> MIDDLE_SHAPE;
            case BASE -> BASE_SHAPE;
        };
        Vec3 offset = state.getOffset(level, pos);
        return shape.move(offset.x, 0, offset.z);
    }

    @Override
    public float getMaxHorizontalOffset() {
        return 0.125F;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    private boolean canAttach(LevelReader level, BlockPos pos, Direction tip) {
        BlockPos behind = pos.relative(tip.getOpposite());
        BlockState support = level.getBlockState(behind);
        return support.isFaceSturdy(level, behind, tip) || isSame(support, tip);
    }

    private boolean isSame(BlockState state, Direction tip) {
        return state.is(this) && state.getValue(TIP_DIRECTION) == tip;
    }

    private DripstoneThickness thickness(LevelReader level, BlockPos pos, Direction tip) {
        BlockState front = level.getBlockState(pos.relative(tip));
        if (!isSame(front, tip)) return DripstoneThickness.TIP;
        DripstoneThickness frontThickness = front.getValue(THICKNESS);
        if (frontThickness == DripstoneThickness.TIP || frontThickness == DripstoneThickness.TIP_MERGE)
            return DripstoneThickness.FRUSTUM;
        return isSame(level.getBlockState(pos.relative(tip.getOpposite())), tip)
                ? DripstoneThickness.MIDDLE
                : DripstoneThickness.BASE;
    }
}
