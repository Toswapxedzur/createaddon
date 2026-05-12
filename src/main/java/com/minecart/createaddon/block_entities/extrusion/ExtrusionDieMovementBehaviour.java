package com.minecart.createaddon.block_entities.extrusion;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExtrusionDieMovementBehaviour implements MovementBehaviour {

    private static final ExtrusionDieMovementBehaviour DROP_DELEGATE = new ExtrusionDieMovementBehaviour();

    private static final String PREV_CELL_KEY = "ExtrusionDiePrevCell";
    private static final String PENDING_DECOMPOSE_KEY = "ExtrusionDiePendingDecompose";

    /**
     * 1.20.1: catnip's {@code NBTHelper#readBlockPos} isn't available; use vanilla {@link NbtUtils}
     * and store BlockPos as a sub-{@link CompoundTag}.
     */
    private static BlockPos readBlockPos(CompoundTag tag, String key) {
        return NbtUtils.readBlockPos(tag.getCompound(key));
    }

    /**
     * 1.20.1: no {@code collectOrDropItem} on {@link MovementBehaviour}; fall back to
     * {@link MovementBehaviour#dropItem(MovementContext, ItemStack)} which drops in-world.
     */
    public static void collectDecomposedDrop(MovementContext context, ItemStack stack) {
        DROP_DELEGATE.dropItem(context, stack);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    public void startMoving(MovementContext context) {
        context.data.remove(PREV_CELL_KEY);
        context.data.remove(PENDING_DECOMPOSE_KEY);
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (!context.world.isClientSide && context.world instanceof ServerLevel sl && context.data.contains(PENDING_DECOMPOSE_KEY)) {
            BlockPos pending = readBlockPos(context.data, PENDING_DECOMPOSE_KEY);
            context.data.remove(PENDING_DECOMPOSE_KEY);
            ExtrusionDieBlockEntity.applyExtrusionDecomposition(sl, context, pending);
        }
        context.data.remove(PREV_CELL_KEY);
        context.data.remove(PENDING_DECOMPOSE_KEY);
    }

    @Override
    public void cancelStall(MovementContext context) {
        context.data.remove(PREV_CELL_KEY);
        context.data.remove(PENDING_DECOMPOSE_KEY);
        MovementBehaviour.super.cancelStall(context);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        Level world = context.world;

        if (world.isClientSide)
            return;

        if (world instanceof ServerLevel sl && context.data.contains(PENDING_DECOMPOSE_KEY)) {
            BlockPos pending = readBlockPos(context.data, PENDING_DECOMPOSE_KEY);
            context.data.remove(PENDING_DECOMPOSE_KEY);
            ExtrusionDieBlockEntity.applyExtrusionDecomposition(sl, context, pending);
        }

        BlockPos prev = context.data.contains(PREV_CELL_KEY) ? readBlockPos(context.data, PREV_CELL_KEY) : null;
        if (prev != null && !prev.equals(pos) && world instanceof ServerLevel) {
            context.data.put(PENDING_DECOMPOSE_KEY, NbtUtils.writeBlockPos(prev));
        }

        context.data.put(PREV_CELL_KEY, NbtUtils.writeBlockPos(pos));
    }
}
