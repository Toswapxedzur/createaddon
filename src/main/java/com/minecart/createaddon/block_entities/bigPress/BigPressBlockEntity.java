package com.minecart.createaddon.block_entities.bigPress;

import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class BigPressBlockEntity extends KineticBlockEntity implements CompressingBehaviour.CompressingBehaviourSpecifics {
    protected CompressingBehaviour compressingBehaviour;
    public int processingTicks;

    public BigPressBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        compressingBehaviour = new CompressingBehaviour(this);
        behaviours.add(compressingBehaviour);
    }

    public Optional<CompressingRecipe> getRecipe(ItemStack input) {
        // SequencedAssemblyRecipe.getRecipe in 1.20.1 returns Optional<R> directly (not RecipeHolder)
        Optional<CompressingRecipe> assemblyRecipe =
                SequencedAssemblyRecipe.getRecipe(getLevel(), input, ModRecipes.COMPRESSING.getType(), CompressingRecipe.class);
        if (assemblyRecipe.isPresent())
            return assemblyRecipe;
        return ModRecipes.COMPRESSING.find(new SimpleContainer(input), getLevel());
    }

    public boolean hasRecipe(ItemStack input) {
        return getRecipe(input).isPresent();
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        Optional<CompressingRecipe> recipe = getRecipe(input.stack);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;

        List<ItemStack> outputs = RecipeApplier.applyRecipeOn(
                getLevel(),
                canProcessInBulk() ? input.stack : input.stack.copyWithCount(1),
                recipe.get(), true
        );

        outputList.addAll(outputs);
        return true;
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack item = itemEntity.getItem();
        Optional<CompressingRecipe> recipe = getRecipe(item);
        if (recipe.isEmpty())
            return false;

        if (simulate)
            return true;

        compressingBehaviour.particleItems.add(item);

        if (canProcessInBulk() || item.getCount() == 1) {
            RecipeApplier.applyRecipeOn(itemEntity, recipe.get(), true);
        } else {
            List<ItemStack> results = RecipeApplier.applyRecipeOn(
                    getLevel(),
                    item.copyWithCount(1),
                    recipe.get(), true
            );

            for (ItemStack result : results) {
                ItemEntity created = new ItemEntity(getLevel(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
                created.setDefaultPickUpDelay();
                created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, getLevel().random, .05f));
                getLevel().addFreshEntity(created);
            }

            item.shrink(1);
            itemEntity.setItem(item);
        }
        return true;
    }

    @Override
    public int getProcessingTime(ItemStack input) {
        Optional<CompressingRecipe> recipe = getRecipe(input);
        if (recipe.isEmpty())
            return 0;
        return recipe.get().getProcessingDuration();
    }

    @Override
    public boolean canProcessInBulk() {
        return AllConfigs.server().recipes.bulkPressing.get();
    }

    @Override
    public void onPressingCompleted() {
    }

    @Override
    public int getParticleAmount() {
        return 30;
    }

    @Override
    public float getKineticSpeed() {
        return getSpeed();
    }

    public CompressingBehaviour getCompressingBehaviour() {
        return compressingBehaviour;
    }
}
