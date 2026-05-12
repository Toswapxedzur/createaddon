package com.minecart.createaddon.block_entities.sieve;

import com.minecart.createaddon.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

public class MechanicalSieveBlock extends HorizontalKineticBlock implements IBE<MechanicalSieveBlockEntity> {
    private static final VoxelShape SHAPE = box(0, 6, 0, 16, 10, 16);

    public MechanicalSieveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred);
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
    public Class<MechanicalSieveBlockEntity> getBlockEntityClass() {
        return MechanicalSieveBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalSieveBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_SIEVE.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    /**
     * 1.20.1 Forge uses a single {@code use} method that takes both ItemStack-bearing and bare hits.
     * Sneak right-click pulls outputs back to the player; right-click with an item inserts one unit.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Sneak right-click (with or without item): drain outputs.
        if (player.isSecondaryUseActive()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            return getBlockEntityOptional(level, pos).map(be -> {
                boolean any = false;
                for (ItemStack out : be.drainOutputs()) {
                    if (out.isEmpty()) continue;
                    ItemHandlerHelper.giveItemToPlayer(player, out);
                    any = true;
                }
                if (any) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                            1.0f + level.getRandom().nextFloat() * 0.2f);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }).orElse(InteractionResult.PASS);
        }

        // Non-sneak with item: try insert one.
        if (stack.isEmpty())
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        return getBlockEntityOptional(level, pos).map(be -> {
            ItemStack one = stack.copyWithCount(1);
            ItemStack remainder = be.tryInsertInput(one, false);
            if (remainder.isEmpty()) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                        1.2f + level.getRandom().nextFloat() * 0.2f);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }).orElse(InteractionResult.PASS);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        tryAcceptItemEntity(level, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        tryAcceptItemEntity(level, entity);
    }

    private static void tryAcceptItemEntity(BlockGetter level, Entity entity) {
        if (!(entity instanceof ItemEntity item)) return;
        if (item.level().isClientSide) return;
        if (!item.isAlive()) return;

        ItemStack stack = item.getItem();
        if (stack.isEmpty()) return;

        BlockPos pos = item.blockPosition();
        if (item.getY() < pos.getY() + 8.0 / 16.0) return;

        if (!(level.getBlockEntity(pos) instanceof MechanicalSieveBlockEntity be)) return;

        ItemStack toInsert = stack.copyWithCount(1);
        ItemStack remainder = be.tryInsertInput(toInsert, false);
        if (!remainder.isEmpty()) return;

        stack.shrink(1);
        if (stack.isEmpty())
            item.discard();
        else
            item.setItem(stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
