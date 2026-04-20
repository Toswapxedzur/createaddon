package com.minecart.createaddon.fluid;

import com.minecart.createaddon.block_entities.labware.LabwareBlockEntity;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Block-entity fluid capability parallel to {@link LabwareItemFluidHandler}: fill/drain toward the scroll
 * {@link LabwareBlockEntity#getLabwareTargetMb()} using the tank amount as actual level. Does not change the scroll;
 * only the backing {@link FluidTank} is updated.
 */
public final class LabwareBlockFluidHandler implements IFluidHandler {
    private final LabwareBlockEntity blockEntity;

    public LabwareBlockFluidHandler(LabwareBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private FluidTank tank() {
        return blockEntity.getLabwareTank();
    }

    private LabwareFluidContents contentView() {
        FluidStack f = tank().getFluid();
        int a = tank().getFluidAmount();
        int t = blockEntity.getLabwareTargetMb();
        if (f.isEmpty() || a <= 0) {
            return new LabwareFluidContents(FluidStack.EMPTY, 0, t);
        }
        return new LabwareFluidContents(f.copyWithAmount(a), a, t);
    }

    private int fillTargetFor(LabwareFluidContents c) {
        if (c.fluid().isEmpty() && c.actualMb() <= 0 && c.targetMb() <= 0) {
            return blockEntity.getLabwareCapacityMb();
        }
        return c.targetMb();
    }

    private void setTankAmount(FluidStack typeSource, int newActualMb) {
        int cap = blockEntity.getLabwareCapacityMb();
        newActualMb = Mth.clamp(newActualMb, 0, cap);
        FluidStack current = tank().getFluid();
        if (typeSource.isEmpty() || newActualMb <= 0) {
            tank().setFluid(FluidStack.EMPTY);
            return;
        }
        if (current.isEmpty()) {
            tank().setFluid(typeSource.copyWithAmount(newActualMb));
        } else {
            tank().setFluid(current.copyWithAmount(newActualMb));
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return contentView().fluidForHandler();
    }

    @Override
    public int getTankCapacity(int tank) {
        return blockEntity.getLabwareCapacityMb();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }

        LabwareFluidContents c = contentView();
        FluidStack currentFluid = c.fluidForHandler();

        if (!currentFluid.isEmpty() && !FluidStack.isSameFluid(currentFluid, resource)) {
            return 0;
        }

        int effectiveTarget = fillTargetFor(c);
        int availableSpace = effectiveTarget - c.actualMb();

        if (availableSpace <= 0) {
            return 0;
        }

        int amountToFill = Math.min(resource.getAmount(), availableSpace);

        if (action.execute()) {
            int newActualMb = c.actualMb() + amountToFill;
            FluidStack type = currentFluid.isEmpty() ? resource : currentFluid;
            setTankAmount(type, newActualMb);
        }

        return amountToFill;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack currentFluid = contentView().fluidForHandler();

        if (currentFluid.isEmpty() || !FluidStack.isSameFluid(currentFluid, resource)) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        LabwareFluidContents c = contentView();
        FluidStack currentFluid = c.fluidForHandler();

        if (currentFluid.isEmpty() || c.actualMb() <= 0) {
            return FluidStack.EMPTY;
        }

        int effectiveTarget = fillTargetFor(c);
        int drainableAboveTarget = c.actualMb() - effectiveTarget;

        if (drainableAboveTarget <= 0) {
            return FluidStack.EMPTY;
        }

        int drainAmount = Math.min(maxDrain, drainableAboveTarget);

        FluidStack out = currentFluid.copyWithAmount(drainAmount);

        if (action.execute()) {
            int remainingMb = c.actualMb() - drainAmount;
            setTankAmount(currentFluid, remainingMb);
        }

        return out;
    }
}
