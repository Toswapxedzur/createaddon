package com.minecart.createaddon.block_entities.extrusion;

import com.minecart.createaddon.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExtrusionDieBlock extends HorizontalKineticBlock implements IBE<ExtrusionDieBlockEntity> {
    private static final VoxelShape SHAPE_Z = Shapes.or(
            Shapes.or(Shapes.or(box(0, 0, 7, 1, 16, 9), box(15, 0, 7, 16, 16, 9)), box(1, 0, 7, 15, 1, 9)),
            box(1, 15, 7, 15, 16, 9));
    private static final VoxelShape SHAPE_X = Shapes.or(
            Shapes.or(Shapes.or(box(7, 0, 0, 9, 16, 1), box(7, 0, 15, 9, 16, 16)), box(7, 0, 1, 9, 1, 15)),
            box(7, 15, 1, 9, 16, 15));

    public ExtrusionDieBlock(Properties properties) {
        super(properties);
    }

    private static VoxelShape shapeFor(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
        return super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<ExtrusionDieBlockEntity> getBlockEntityClass() {
        return ExtrusionDieBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExtrusionDieBlockEntity> getBlockEntityType() {
        return ModBlockEntities.EXTRUSION_DIE.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
