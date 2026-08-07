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
import net.minecraft.world.ItemInteractionResult;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Frame matches {@code mechanical_sieve/block}: a hollow casing 4-pixel-tall (y 6→10)
 * with shaft stubs poking out both ends of the {@link HorizontalKineticBlock#HORIZONTAL_FACING} axis.
 *
 * <p>Right-click with an item drops 1 unit into the input slot when empty; sneak right-click
 * pulls all output items back into the player.
 */
public class MechanicalSieveBlock extends HorizontalKineticBlock implements IBE<MechanicalSieveBlockEntity> {
    private static final VoxelShape SHAPE = box(0, 6, 0, 16, 10, 16);

    public MechanicalSieveBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        return getBlockEntityOptional(level, pos).map(be -> {
            ItemStack one = stack.copyWithCount(1);
            ItemStack remainder = be.tryInsertInput(one, false);
            if (remainder.isEmpty()) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                        1.2f + level.getRandom().nextFloat() * 0.2f);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

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

    /**
     * Called when an entity finishes falling onto the sieve. If it's an item entity landing
     * on the top face, try to auto-insert it into the input slot.
     */
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        tryAcceptItemEntity(level, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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
        // Item must be roughly above the sieve mesh (y=8), not under it.
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
        // SmartBlockEntity.destroy() drops contents via ItemHelper.dropContents (see BE).
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
