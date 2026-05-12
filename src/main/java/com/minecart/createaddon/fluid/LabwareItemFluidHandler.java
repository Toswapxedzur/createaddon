package com.minecart.createaddon.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

/**
 * 1.20.1 Forge port. Storage backed by an NBT subcompound on the item-stack
 * (see {@link LabwareFluidContents}).
 */
public final class LabwareItemFluidHandler implements IFluidHandlerItem {
    private static final String CONTENTS_KEY = "LabwareContents";

    private final ItemStack container;
    private final int capacityMb;

    public LabwareItemFluidHandler(ItemStack container, int capacityMb) {
        this.container = container;
        this.capacityMb = capacityMb;
    }

    private LabwareFluidContents getContent() {
        CompoundTag tag = container.getTag();
        if (tag == null || !tag.contains(CONTENTS_KEY)) return LabwareFluidContents.EMPTY;
        return LabwareFluidContents.read(tag.getCompound(CONTENTS_KEY));
    }

    private void apply(FluidStack fluid, int actualMb, int targetMb) {
        actualMb = Mth.clamp(actualMb, 0, capacityMb);
        targetMb = Mth.clamp(targetMb, 0, capacityMb);
        if (fluid.isEmpty() || actualMb <= 0) {
            fluid = FluidStack.EMPTY;
            actualMb = 0;
        } else {
            FluidStack copy = fluid.copy();
            copy.setAmount(actualMb);
            fluid = copy;
        }
        LabwareFluidContents contents = new LabwareFluidContents(fluid, actualMb, targetMb);
        container.getOrCreateTag().put(CONTENTS_KEY, contents.write());
    }

    public boolean canBeEmptied() {
        LabwareFluidContents c = getContent();
        return c.actualMb() > c.targetMb();
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return getContent().fluidForHandler();
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacityMb;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }

        LabwareFluidContents c = getContent();
        FluidStack currentFluid = c.fluidForHandler();

        if (!currentFluid.isEmpty() && !currentFluid.isFluidEqual(resource)) {
            return 0;
        }

        int effectiveTarget = c.targetMb();
        int availableSpace = effectiveTarget - c.actualMb();

        if (availableSpace < 0) {
            return 0;
        }

        int amountToFill = Math.min(resource.getAmount(), availableSpace);

        if (action.execute()) {
            int newActualMb = c.actualMb() + amountToFill;
            int newTargetMb = c.targetMb() > 0 ? c.targetMb() : effectiveTarget;
            apply(resource, newActualMb, newTargetMb);
        }

        return amountToFill;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack currentFluid = getContent().fluidForHandler();
        if (currentFluid.isEmpty() || !currentFluid.isFluidEqual(resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        LabwareFluidContents c = getContent();
        FluidStack currentFluid = c.fluidForHandler();

        if (currentFluid.isEmpty() || c.actualMb() <= 0) {
            return FluidStack.EMPTY;
        }

        int effectiveTarget = c.targetMb();
        int availableSpace = c.actualMb() - effectiveTarget;
        if (availableSpace <= 0) {
            return FluidStack.EMPTY;
        }

        int drainAmount = Math.min(maxDrain, availableSpace);
        FluidStack out = currentFluid.copy();
        out.setAmount(drainAmount);

        if (action.execute()) {
            int remainingMb = c.actualMb() - drainAmount;
            apply(currentFluid, remainingMb, c.targetMb());
        }

        return out;
    }
}
