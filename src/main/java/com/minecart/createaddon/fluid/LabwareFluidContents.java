package com.minecart.createaddon.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.fluids.FluidStack;

/**
 * 1.20.1 port: was a 1.21.x DataComponent. Now a plain POJO serialized into/from
 * an item-stack NBT subcompound. Keys (actual / target / fluid) are accessed via
 * {@link #write} and {@link #read}.
 */
public record LabwareFluidContents(FluidStack fluid, int actualMb, int targetMb) {
    public static final LabwareFluidContents EMPTY = new LabwareFluidContents(FluidStack.EMPTY, 0, 0);

    public static final String KEY_FLUID = "Fluid";
    public static final String KEY_ACTUAL = "ActualMb";
    public static final String KEY_TARGET = "TargetMb";

    public LabwareFluidContents {
        if (fluid.isEmpty() || actualMb <= 0) {
            fluid = FluidStack.EMPTY;
            actualMb = 0;
        } else {
            FluidStack copy = fluid.copy();
            copy.setAmount(actualMb);
            fluid = copy;
        }
        if (targetMb < 0) targetMb = 0;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        if (!fluid.isEmpty()) {
            tag.put(KEY_FLUID, fluid.writeToNBT(new CompoundTag()));
        }
        tag.putInt(KEY_ACTUAL, actualMb);
        tag.putInt(KEY_TARGET, targetMb);
        return tag;
    }

    public static LabwareFluidContents read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return EMPTY;
        FluidStack fluid = tag.contains(KEY_FLUID) ? FluidStack.loadFluidStackFromNBT(tag.getCompound(KEY_FLUID)) : FluidStack.EMPTY;
        int actual = tag.contains(KEY_ACTUAL) ? tag.getInt(KEY_ACTUAL) : (fluid.isEmpty() ? 0 : fluid.getAmount());
        int target = tag.getInt(KEY_TARGET);
        return new LabwareFluidContents(fluid, actual, target);
    }

    /** Returns a copy of the fluid with amount set to {@link #actualMb}. */
    public FluidStack fluidForHandler() {
        if (fluid.isEmpty()) return FluidStack.EMPTY;
        FluidStack copy = fluid.copy();
        copy.setAmount(actualMb);
        return copy;
    }

    public static LabwareFluidContents clamp(LabwareFluidContents c, int capacity) {
        int actual = Mth.clamp(c.actualMb(), 0, capacity);
        int target = Mth.clamp(c.targetMb(), 0, capacity);
        return new LabwareFluidContents(c.fluid(), actual, target);
    }
}
