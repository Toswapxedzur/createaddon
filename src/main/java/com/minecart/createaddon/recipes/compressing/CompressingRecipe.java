package com.minecart.createaddon.recipes.compressing;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 1.20.1 port: extends {@code ProcessingRecipe<SimpleContainer>} (was {@code StandardProcessingRecipe<SingleRecipeInput>}).
 */
public class CompressingRecipe extends ProcessingRecipe<SimpleContainer> implements IAssemblyRecipe {
    public CompressingRecipe(ProcessingRecipeParams params) {
        super(ModRecipes.COMPRESSING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean supportsAssembly() {
        return true;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0).test(inv.getItem(0));
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

    /**
     * Two layers of {@link Supplier}: the outer one is held by the recipe even when JEI is
     * absent at runtime, the inner one is invoked client-side only when JEI builds the
     * sequenced-assembly UI. Wrapping the {@code AssemblyCompressing} reference in two
     * suppliers keeps the JEI class out of the recipe-loading classpath on dedicated servers
     * / when JEI isn't installed.
     */
    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> com.minecart.createaddon.jei.category.sequencedAssembly.AssemblyCompressing::new;
    }
}
