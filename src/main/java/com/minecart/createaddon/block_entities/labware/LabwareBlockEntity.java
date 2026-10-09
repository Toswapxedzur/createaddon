package com.minecart.createaddon.block_entities.labware;

import com.minecart.createaddon.ModDataComponents;
import com.minecart.createaddon.block_entities.behaviour.DripCollectorBehaviour;
import com.minecart.createaddon.block_entities.behaviour.LabwareScrollValueBehaviour;
import com.minecart.createaddon.fluid.LabwareBlockFluidHandler;
import com.minecart.createaddon.fluid.LabwareFluidContents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;

public abstract class LabwareBlockEntity extends SmartBlockEntity {
    private static final String TANK_KEY = "CreateAddonFluid";

    protected LabwareScrollValueBehaviour fillLevel;
    protected DripCollectorBehaviour dripCollector;
    protected final LabwareFluidTank tank;
    private final int capacityMb;
    private final int step;

    protected LabwareBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacityMb, int milestone, int step) {
        super(type, pos, state);
        this.capacityMb = capacityMb;
        this.step = step;
        this.tank = new LabwareFluidTank(capacityMb);
    }

    protected abstract Component scrollLabel();

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        dripCollector = new DripCollectorBehaviour(this);
        behaviours.add(dripCollector);
    }

    @Override
    public void initialize() {
        super.initialize();
        fillLevel.setStep(step);
        fillLevel.between(0, capacityMb);
        fillLevel.withFormatter(v -> v + " / " + capacityMb);
        dripCollector.setFluidDripCallback(this::onDripstoneFluidDrip);
    }

    /** One drip adds one scroll step of fluid, capped by {@link LabwareBlockFluidHandler#fill} to the fill target. */
    private void onDripstoneFluidDrip(ServerLevel level, BlockPos collectorPos, Fluid fluid) {
        if (!collectorPos.equals(getBlockPos()) || fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        FluidStack offer = new FluidStack(fluid, 5);
        getFluidHandler().fill(offer, IFluidHandler.FluidAction.EXECUTE);
    }

    private void onTankChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            sendData();
            level.invalidateCapabilities(worldPosition);
        }
    }

    public IFluidHandler getFluidHandler() {
        return new LabwareBlockFluidHandler(this);
    }

    /** @see LabwareBlockFluidHandler */
    public FluidTank getLabwareTank() {
        return tank;
    }

    /** Scroll / fill target in mB (parallel to {@link LabwareFluidContents#targetMb()} on items). */
    public int getLabwareTargetMb() {
        return fillLevel != null ? fillLevel.value : 0;
    }

    public FluidStack getTankFluidForRender() {
        return tank.getFluid().copy();
    }

    public int getLabwareCapacityMb() {
        return capacityMb;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        tank.readFromNBT(registries, tag);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tank.writeToNBT(registries, tag);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        int target = fillLevel != null ? fillLevel.value : 0;
        int actual = tank.getFluidAmount();
        FluidStack fluid = tank.isEmpty() ? FluidStack.EMPTY : tank.getFluid().copyWithAmount(actual);
        components.set(ModDataComponents.LABWARE_CONTENTS.get(), new LabwareFluidContents(fluid, actual, target));
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        LabwareFluidContents contents = input.get(ModDataComponents.LABWARE_CONTENTS.get());
        if (contents == null) {
            tank.setFluid(FluidStack.EMPTY);
            if (fillLevel != null) {
                fillLevel.setStoredTarget(0);
            }
            return;
        }
        int actual = Math.min(contents.actualMb(), capacityMb);
        if (!contents.fluid().isEmpty() && actual > 0) {
            tank.setFluid(contents.fluid().copyWithAmount(actual));
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
        if (fillLevel != null) {
            fillLevel.setStoredTarget(contents.targetMb());
        }
    }

    private final class LabwareFluidTank extends FluidTank {
        LabwareFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            LabwareBlockEntity.this.onTankChanged();
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(stack);
            LabwareBlockEntity.this.onTankChanged();
        }
    }
}
