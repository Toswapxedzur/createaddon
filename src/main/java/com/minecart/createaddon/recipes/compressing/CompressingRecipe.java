package com.minecart.createaddon.recipes.compressing;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.jei.category.sequencedAssembly.AssemblyCompressing;
import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class CompressingRecipe extends StandardProcessingRecipe<SingleRecipeInput> implements IAssemblyRecipe {
    public CompressingRecipe(ProcessingRecipeParams params) {
        super(ModRecipes.COMPRESSING, params);
    }

    @Override
    public boolean supportsAssembly() {
        return true;
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SingleRecipeInput singleRecipeInput, Level level) {
        if (singleRecipeInput.isEmpty())
            return false;
        return ingredients.get(0)
                .test(singleRecipeInput.getItem(0));
    }

    @Override
    public Component getDescriptionForAssembly() {
        return Component.translatable("recipe.createaddon.compressing");
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(ModBlocks.BIGPRESS.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> AssemblyCompressing::new;
    }
}
