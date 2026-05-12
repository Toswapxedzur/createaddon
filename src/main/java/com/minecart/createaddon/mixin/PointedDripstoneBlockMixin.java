package com.minecart.createaddon.mixin;

import com.minecart.createaddon.block_entities.behaviour.DripCollectorBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {

    @Shadow
    @Nullable
    private static BlockPos findTip(BlockState state, LevelAccessor level, BlockPos pos, int maxIterations, boolean isTipMerge) {
        throw new AssertionError();
    }

    @Shadow
    public static Fluid getCauldronFillFluidType(ServerLevel level, BlockPos pos) {
        throw new AssertionError();
    }

    @Redirect(
            method = "maybeTransferFluid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;findTip(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"
            )
    )
    private static BlockPos createaddon$notifyCollectorBelowTip(BlockState state, LevelAccessor level, BlockPos pos, int maxIterations, boolean isTipMerge) {
        BlockPos tipPos = findTip(state, level, pos, maxIterations, isTipMerge);
        if (tipPos == null || !(level instanceof ServerLevel serverLevel)) {
            return tipPos;
        }

        Fluid fluid = getCauldronFillFluidType(serverLevel, pos);
        if (fluid == Fluids.EMPTY) {
            return tipPos;
        }

        BlockPos collectorPos = tipPos.below();
        DripCollectorBehaviour behaviour = BlockEntityBehaviour.get(serverLevel, collectorPos, DripCollectorBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.onFluidDrip(serverLevel, collectorPos, fluid);
        }

        return tipPos;
    }
}
