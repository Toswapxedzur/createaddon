package com.minecart.createaddon.block_entities.sieve;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.Optional;

/**
 * Kinetic sieve modeled after {@code com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity}:
 * separate {@code inputInv} (1 slot, max 1 item) and {@code outputInv} (9 slots), exposed via a
 * {@link CombinedInvWrapper} that only allows external insertion to the input and external
 * extraction from the outputs. Processing pauses if any output slot is full so finished
 * products accumulate inside until removed.
 */
public class MechanicalSieveBlockEntity extends KineticBlockEntity implements Clearable {

    /** RPM at which particles emit at exactly one per game tick. */
    private static final float PARTICLE_REFERENCE_RPM = 64f;

    public final ItemStackHandler inputInv;
    public final ItemStackHandler outputInv;

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
    }

    /**
     * Marks the BE dirty and pushes a sync packet to clients whenever either inventory
     * mutates. Without this, server-side hopper / right-click / drop-on-top inserts only
     * surface on the client after a chunk reload (quit + rejoin).
     */
    private void onInventoryChanged() {
        if (level == null || level.isClientSide)
            return;
        setChanged();
        sendData();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MECHANICAL_SIEVE.get(),
                // Fresh wrapper per query — only enforces direction (in→input, out←outputs).
                (be, ctx) -> new SieveInventoryHandler(be.inputInv, be.outputInv)
        );
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

        // Pause if any output slot is full — completed results stay inside the sieve.
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
            Optional<RecipeHolder<SieveRecipe>> recipe = ModRecipes.SIEVING.find(inv, level);
            if (recipe.isEmpty()) {
                timer = 100;
                sendData();
            } else {
                lastRecipe = recipe.get().value();
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
            Optional<RecipeHolder<SieveRecipe>> recipe = ModRecipes.SIEVING.find(inv, level);
            if (recipe.isEmpty())
                return;
            lastRecipe = recipe.get().value();
        }

        ItemStack input = inputInv.getStackInSlot(0);
        ItemStack remainder = input.getCraftingRemainingItem();
        input.shrink(1);
        inputInv.setStackInSlot(0, input);

        lastRecipe.rollResults(level.random)
                .forEach(stack -> ItemHandlerHelper.insertItemStacked(outputInv, stack, false));
        if (!remainder.isEmpty())
            ItemHandlerHelper.insertItemStacked(outputInv, remainder, false);

        sendData();
        setChanged();
    }

    /**
     * Direct hook for {@link MechanicalSieveBlock#useItemOn} and the drop-onto-top path.
     * Bypasses the capability wrapper so behavior doesn't depend on recipe loading state —
     * the input slot's own limit (1) is the only constraint.
     */
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
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
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
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Timer", timer);
        tag.put("InputInventory", inputInv.serializeNBT(registries));
        tag.put("OutputInventory", outputInv.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        timer = tag.getInt("Timer");
        inputInv.deserializeNBT(registries, tag.getCompound("InputInventory"));
        outputInv.deserializeNBT(registries, tag.getCompound("OutputInventory"));
    }

    /**
     * Combined view exposed via {@link Capabilities.ItemHandler}. Direction-only:
     * external code may only insert into the input handler and may only extract
     * from the output handler. No recipe-validity check — invalid items just sit
     * in the input slot until the player removes them.
     */
    private static class SieveInventoryHandler extends CombinedInvWrapper {
        private final ItemStackHandler outputInvRef;

        SieveInventoryHandler(ItemStackHandler inputInv, ItemStackHandler outputInv) {
            super(inputInv, outputInv);
            this.outputInvRef = outputInv;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (outputInvRef == getHandlerFromIndex(getIndexForSlot(slot)))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (outputInvRef != getHandlerFromIndex(getIndexForSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    }

    /** Drains every output slot via the underlying handler (bypasses external extract block). */
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
