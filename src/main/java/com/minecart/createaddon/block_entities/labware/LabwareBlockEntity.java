package com.minecart.createaddon.block_entities.labware;

import com.minecart.createaddon.block_entities.behaviour.DripCollectorBehaviour;
import com.minecart.createaddon.block_entities.behaviour.LabwareScrollValueBehaviour;
import com.minecart.createaddon.fluid.LabwareBlockFluidHandler;
import com.minecart.createaddon.fluid.LabwareFluidContents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.20.1 port. Replaces 1.21.x data components ({@code collectImplicitComponents}/
 * {@code applyImplicitComponents}) with explicit item-NBT serialization via
 * {@link #writeContentsToItem} and {@link #readContentsFromItem}. The block's
 * fluid handler capability is exposed through Forge's
 * {@link #getCapability(Capability, Direction)} override.
 */
public abstract class LabwareBlockEntity extends SmartBlockEntity {
    private static final String CONTENTS_KEY = "LabwareContents";

    protected LabwareScrollValueBehaviour fillLevel;
    protected DripCollectorBehaviour dripCollector;
    protected final LabwareFluidTank tank;
    private final int capacityMb;
    private final int step;
    private final LazyOptional<IFluidHandler> fluidHandlerCap;

    protected LabwareBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacityMb, int milestone, int step) {
        super(type, pos, state);
        this.capacityMb = capacityMb;
        this.step = step;
        this.tank = new LabwareFluidTank(capacityMb);
        this.fluidHandlerCap = LazyOptional.of(() -> new LabwareBlockFluidHandler(this));
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
        }
    }

    public IFluidHandler getFluidHandler() {
        return new LabwareBlockFluidHandler(this);
    }

    public FluidTank getLabwareTank() {
        return tank;
    }

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
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tank.readFromNBT(tag);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tank.writeToNBT(tag);
    }

    /**
     * Persists fluid + target into the item-stack NBT so block→item→block round trips preserve state.
     * Called from {@link com.minecart.createaddon.block.labware.LabwareBlock#getCloneItemStack} or similar.
     */
    public void writeContentsToItem(ItemStack stack) {
        int target = fillLevel != null ? fillLevel.value : 0;
        int actual = tank.getFluidAmount();
        FluidStack fluid;
        if (tank.isEmpty()) {
            fluid = FluidStack.EMPTY;
        } else {
            fluid = tank.getFluid().copy();
            fluid.setAmount(actual);
        }
        LabwareFluidContents contents = new LabwareFluidContents(fluid, actual, target);
        stack.getOrCreateTag().put(CONTENTS_KEY, contents.write());
    }

    /** Inverse of {@link #writeContentsToItem}. Restores tank + scroll target from item NBT. */
    public void readContentsFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CONTENTS_KEY)) {
            tank.setFluid(FluidStack.EMPTY);
            if (fillLevel != null) fillLevel.setStoredTarget(0);
            return;
        }
        LabwareFluidContents contents = LabwareFluidContents.read(tag.getCompound(CONTENTS_KEY));
        int actual = Math.min(contents.actualMb(), capacityMb);
        if (!contents.fluid().isEmpty() && actual > 0) {
            FluidStack copy = contents.fluid().copy();
            copy.setAmount(actual);
            tank.setFluid(copy);
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
        if (fillLevel != null) {
            fillLevel.setStoredTarget(contents.targetMb());
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidHandlerCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandlerCap.invalidate();
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
