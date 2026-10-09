package com.minecart.createaddon.block_entities.extrusion;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * When the active point enters a new block cell, the <em>previous</em> cell is queued for decomposition and is only
 * processed on the <em>next</em> cell transition, so the block breaks after the extruder has moved further past it.
 * Bare-hand breakable + cache match required. The contraption never stalls for breaking.
 */
public class ExtrusionDieMovementBehaviour implements MovementBehaviour {

    private static final ExtrusionDieMovementBehaviour DROP_DELEGATE = new ExtrusionDieMovementBehaviour();

    private static final String PREV_CELL_KEY = "ExtrusionDiePrevCell";
    private static final String PENDING_DECOMPOSE_KEY = "ExtrusionDiePendingDecompose";

    /** Routes drops through Create actor item handling (mounted storage vs world). */
    public static void collectDecomposedDrop(MovementContext context, ItemStack stack) {
        DROP_DELEGATE.collectOrDropItem(context, stack);
    }

    /**
     * Shifts the tracked point 0.5 blocks along {@link MovementContext#relativeMotion} (contraption-local velocity),
     * so decomposition follows travel direction in both directions.
     */
//    @Override
//    public Vec3 getActiveAreaOffset(MovementContext context) {
//        Vec3 v = context.relativeMotion;
//        double lenSq = v.lengthSqr();
//        if (lenSq < 1e-12)
//            return Vec3.ZERO;
//        return v.scale(0.5 / Math.sqrt(lenSq));
//    }

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
            BlockPos pending = NBTHelper.readBlockPos(context.data, PENDING_DECOMPOSE_KEY);
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
            BlockPos pending = NBTHelper.readBlockPos(context.data, PENDING_DECOMPOSE_KEY);
            context.data.remove(PENDING_DECOMPOSE_KEY);
            ExtrusionDieBlockEntity.applyExtrusionDecomposition(sl, context, pending);
        }

        BlockPos prev = context.data.contains(PREV_CELL_KEY) ? NBTHelper.readBlockPos(context.data, PREV_CELL_KEY) : null;
        if (prev != null && !prev.equals(pos) && world instanceof ServerLevel) {
            context.data.put(PENDING_DECOMPOSE_KEY, NbtUtils.writeBlockPos(prev));
        }

        context.data.put(PREV_CELL_KEY, NbtUtils.writeBlockPos(pos));
    }
}
