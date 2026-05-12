package com.minecart.createaddon.fluid;

import com.minecart.createaddon.block_entities.labware.LabwareBlockEntity;
import net.minecraft.util.Mth;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

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
        FluidStack copy = f.copy();
        copy.setAmount(a);
        return new LabwareFluidContents(copy, a, t);
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
        FluidStack next = current.isEmpty() ? typeSource.copy() : current.copy();
        next.setAmount(newActualMb);
        tank().setFluid(next);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return contentView().fluidForHandler();
    }

    @Override
    public int getTankCapacity(int tank) {
        return blockEntity.getLabwareCapacityMb();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }

        LabwareFluidContents c = contentView();
        FluidStack currentFluid = c.fluidForHandler();

        if (!currentFluid.isEmpty() && !currentFluid.isFluidEqual(resource)) {
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
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack currentFluid = contentView().fluidForHandler();
        if (currentFluid.isEmpty() || !currentFluid.isFluidEqual(resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
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
        FluidStack out = currentFluid.copy();
        out.setAmount(drainAmount);

        if (action.execute()) {
            int remainingMb = c.actualMb() - drainAmount;
            setTankAmount(currentFluid, remainingMb);
        }

        return out;
    }
}
