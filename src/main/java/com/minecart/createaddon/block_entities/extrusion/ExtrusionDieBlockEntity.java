package com.minecart.createaddon.block_entities.extrusion;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class ExtrusionDieBlockEntity extends KineticBlockEntity {

    public ExtrusionDieBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(0.125);
    }

    public static boolean isBreakableByBareHand(BlockState state) {
        return !state.requiresCorrectToolForDrops();
    }

    public static void applyExtrusionDecomposition(ServerLevel level, MovementContext context, BlockPos targetPos) {
        BlockState state = level.getBlockState(targetPos);
        if (state.isAir())
            return;
        if (!isBreakableByBareHand(state))
            return;

        Optional<ItemStack> decomposed = ExtrusionDieDecompositionCache.lookup(level, state);
        if (decomposed.isEmpty())
            return;

        FluidState fluidState = level.getFluidState(targetPos);

        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, targetPos, Block.getId(state));

        if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !level.restoringBlockSnapshots)
            ExtrusionDieMovementBehaviour.collectDecomposedDrop(context, decomposed.get());

        // 1.20.1: spawnAfterBreak(BlockState, ServerLevel, BlockPos, ItemStack, boolean)
        state.spawnAfterBreak(level, targetPos, ItemStack.EMPTY, false);
        level.setBlockAndUpdate(targetPos, fluidState.createLegacyBlock());
    }
}
