package com.minecart.createaddon.recipes.sieving;

import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * Standard processing recipe for the {@code Mechanical Sieve}: 1 input ingredient,
 * up to 9 chance-based outputs, custom duration. Uses {@link RecipeWrapper} to
 * mirror the inventory wrapping pattern used by Create's millstone/crusher.
 */
public class SieveRecipe extends StandardProcessingRecipe<RecipeWrapper> {
    public SieveRecipe(ProcessingRecipeParams params) {
        super(ModRecipes.SIEVING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 9;
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
