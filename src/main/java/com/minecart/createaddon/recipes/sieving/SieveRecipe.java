package com.minecart.createaddon.recipes.sieving;

import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * 1.20.1 port: parent class is {@code ProcessingRecipe<RecipeWrapper>} (no {@code StandardProcessingRecipe}
 * — that was introduced in 1.21.x).
 */
public class SieveRecipe extends ProcessingRecipe<RecipeWrapper> {
    public SieveRecipe(ProcessingRecipeParams params) {
        super(ModRecipes.SIEVING, params);
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
    public boolean matches(RecipeWrapper inv, Level level) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0).test(inv.getItem(0));
    }
}
