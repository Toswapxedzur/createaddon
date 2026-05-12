package com.minecart.createaddon.block_entities.sieve;

import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MechanicalSieveBlockEntity extends KineticBlockEntity implements Clearable {

    /** RPM at which particles emit at exactly one per game tick. */
    private static final float PARTICLE_REFERENCE_RPM = 64f;

    public final ItemStackHandler inputInv;
    public final ItemStackHandler outputInv;
    private final LazyOptional<IItemHandler> capability;

    public int timer;
    private SieveRecipe lastRecipe;

    public MechanicalSieveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inputInv = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                onInventoryChanged();
            }
        };
        outputInv = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                onInventoryChanged();
            }
        };
        capability = LazyOptional.of(() -> new SieveInventoryHandler(inputInv, outputInv));
    }

    private void onInventoryChanged() {
        if (level == null || level.isClientSide)
            return;
        setChanged();
        sendData();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(0.25);
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;

        for (int i = 0; i < outputInv.getSlots(); i++)
            if (outputInv.getStackInSlot(i).getCount() == outputInv.getSlotLimit(i))
                return;

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (level.isClientSide) {
                spawnParticles();
                return;
            }
            if (timer <= 0)
                process();
            return;
        }

        if (inputInv.getStackInSlot(0).isEmpty())
            return;

        RecipeWrapper inv = new RecipeWrapper(inputInv);
        if (lastRecipe == null || !lastRecipe.matches(inv, level)) {
            Optional<SieveRecipe> recipe = ModRecipes.SIEVING.find(inv, level);
            if (recipe.isEmpty()) {
                timer = 100;
                sendData();
            } else {
                lastRecipe = recipe.get();
                timer = Math.max(1, lastRecipe.getProcessingDuration());
                sendData();
            }
            return;
        }

        timer = Math.max(1, lastRecipe.getProcessingDuration());
        sendData();
    }

    private void process() {
        RecipeWrapper inv = new RecipeWrapper(inputInv);
        if (lastRecipe == null || !lastRecipe.matches(inv, level)) {
            Optional<SieveRecipe> recipe = ModRecipes.SIEVING.find(inv, level);
            if (recipe.isEmpty())
                return;
            lastRecipe = recipe.get();
        }

        ItemStack input = inputInv.getStackInSlot(0);
        ItemStack remainder = input.getCraftingRemainingItem();
        input.shrink(1);
        inputInv.setStackInSlot(0, input);

        lastRecipe.rollResults()
                .forEach(stack -> ItemHandlerHelper.insertItemStacked(outputInv, stack, false));
        if (!remainder.isEmpty())
            ItemHandlerHelper.insertItemStacked(outputInv, remainder, false);

        sendData();
        setChanged();
    }

    public ItemStack tryInsertInput(ItemStack stack, boolean simulate) {
        return inputInv.insertItem(0, stack, simulate);
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    private void spawnParticles() {
        if (level == null) return;
        ItemStack input = inputInv.getStackInSlot(0);
        if (input.isEmpty()) return;

        float speedAbs = Math.abs(getSpeed());
        if (speedAbs == 0) return;

        float perTick = speedAbs / PARTICLE_REFERENCE_RPM;
        int whole = (int) perTick;
        int n = whole + (level.random.nextFloat() < (perTick - whole) ? 1 : 0);
        if (n <= 0) return;

        ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, input);
        BlockPos pos = getBlockPos();
        for (int i = 0; i < n; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 0.7;
            double dz = (level.random.nextDouble() - 0.5) * 0.7;
            double vy = -0.05 - level.random.nextDouble() * 0.05;
            level.addParticle(data,
                    pos.getX() + 0.5 + dx,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + dz,
                    0, vy, 0);
        }
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inputInv.getSlots(); i++) inputInv.setStackInSlot(i, ItemStack.EMPTY);
        for (int i = 0; i < outputInv.getSlots(); i++) outputInv.setStackInSlot(i, ItemStack.EMPTY);
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, inputInv);
        ItemHelper.dropContents(level, worldPosition, outputInv);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Timer", timer);
        tag.put("InputInventory", inputInv.serializeNBT());
        tag.put("OutputInventory", outputInv.serializeNBT());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        timer = tag.getInt("Timer");
        inputInv.deserializeNBT(tag.getCompound("InputInventory"));
        outputInv.deserializeNBT(tag.getCompound("OutputInventory"));
    }

    /** Forge 1.20.1 capability pattern: hand out the combined inventory handler. */
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return capability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capability.invalidate();
    }

    /**
     * Combined view: external code may only insert into the input handler and may only extract
     * from the output handler.
     */
    private static class SieveInventoryHandler extends CombinedInvWrapper {
        private final ItemStackHandler outputInvRef;

        SieveInventoryHandler(ItemStackHandler inputInv, ItemStackHandler outputInv) {
            super(inputInv, outputInv);
            this.outputInvRef = outputInv;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (outputInvRef == getHandlerFromIndex(getIndexForSlot(slot)))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (outputInvRef != getHandlerFromIndex(getIndexForSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    }

    public java.util.List<ItemStack> drainOutputs() {
        java.util.List<ItemStack> drained = new java.util.ArrayList<>();
        for (int i = 0; i < outputInv.getSlots(); i++) {
            ItemStack stack = outputInv.extractItem(i, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) drained.add(stack);
        }
        if (!drained.isEmpty()) {
            sendData();
            setChanged();
        }
        return drained;
    }
}
