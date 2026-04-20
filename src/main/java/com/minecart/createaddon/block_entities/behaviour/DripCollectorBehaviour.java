package com.minecart.createaddon.block_entities.behaviour;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class DripCollectorBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<DripCollectorBehaviour> TYPE = new BehaviourType<>();

    @FunctionalInterface
    public interface FluidDripCallback {
        void accept(ServerLevel level, BlockPos collectorPos, Fluid fluid);
    }

    private static final FluidDripCallback NOOP = (level, collectorPos, fluid) -> {
    };

    private FluidDripCallback fluidDripCallback = NOOP;

    public DripCollectorBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<DripCollectorBehaviour> getType() {
        return TYPE;
    }

    public void setFluidDripCallback(FluidDripCallback fluidDripCallback) {
        this.fluidDripCallback = Objects.requireNonNullElse(fluidDripCallback, NOOP);
    }

    public void onFluidDrip(ServerLevel level, BlockPos collectorPos, Fluid fluid) {
        fluidDripCallback.accept(level, collectorPos, fluid);
    }
}
