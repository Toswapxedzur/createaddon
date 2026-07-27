package com.minecart.createaddon.fluid;

import com.minecart.createaddon.ModDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Item-stack fluid capability: fill/drain toward {@link LabwareFluidContents#targetMb()} using {@link LabwareFluidContents#actualMb()} as the live level.
 * When empty with target 0, fills may use the full {@link #capacityMb} as the effective target (fresh stacks).
 */
public final class LabwareItemFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;
    private final int capacityMb;

    public LabwareItemFluidHandler(ItemStack container, int capacityMb) {
        this.container = container;
        this.capacityMb = capacityMb;
    }

    private LabwareFluidContents getContent() {
        return container.getOrDefault(ModDataComponents.LABWARE_CONTENTS.get(), LabwareFluidContents.EMPTY);
    }

    private void apply(FluidStack fluid, int actualMb, int targetMb) {
        actualMb = Mth.clamp(actualMb, 0, capacityMb);
        targetMb = Mth.clamp(targetMb, 0, capacityMb);
        if (fluid.isEmpty() || actualMb <= 0) {
            fluid = FluidStack.EMPTY;
            actualMb = 0;
        } else {
            fluid = fluid.copyWithAmount(actualMb);
        }
        container.set(ModDataComponents.LABWARE_CONTENTS.get(), new LabwareFluidContents(fluid, actualMb, targetMb));
    }

    /**
     * {@code true} when {@link #fill} could accept fluid (room below the effective fill target and a valid stack).
     */
    public boolean canBeEmptied() {
        LabwareFluidContents c = getContent();
        int effectiveTarget = c.targetMb();
        return c.actualMb() > effectiveTarget;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getContent().fluidForHandler();
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacityMb;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }

        LabwareFluidContents c = getContent();
        FluidStack currentFluid = c.fluidForHandler();

        if (!currentFluid.isEmpty() && !FluidStack.isSameFluid(currentFluid, resource)) {
            return 0;
        }

        int effectiveTarget = c.targetMb() > 0 ? c.targetMb() : capacityMb;
        int availableSpace = effectiveTarget - c.actualMb();

        if (availableSpace <= 0) {
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
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack currentFluid = getContent().fluidForHandler();

        if (currentFluid.isEmpty() || !FluidStack.isSameFluid(currentFluid, resource)) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
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

        // 3. Calculate how much we can actually drain (limited by maxDrain or what's left in the beaker)
        int drainAmount = Math.min(maxDrain, availableSpace);

        // Output stack representing what was successfully drained
        FluidStack out = currentFluid.copyWithAmount(drainAmount);

        // 4. If we are executing (not just simulating), apply the new state
        if (action.execute()) {
            int remainingMb = c.actualMb() - drainAmount;
            // Target remains the same, we just reduce the actual fluid level
            apply(currentFluid, remainingMb, c.targetMb());
        }

        return out;
    }
}
