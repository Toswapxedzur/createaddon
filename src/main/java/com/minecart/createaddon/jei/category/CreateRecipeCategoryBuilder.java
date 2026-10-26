package com.minecart.createaddon.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

public class CreateRecipeCategoryBuilder<T extends Recipe<?>> extends CreateRecipeCategory.Builder<T> {
    public CreateRecipeCategoryBuilder(Class<? extends T> recipeClass) {
        super(recipeClass);
    }

    public static <T extends Recipe<? extends RecipeInput>> CreateRecipeCategoryBuilder builder(Class<T> recipeClass){
        return new CreateRecipeCategoryBuilder(recipeClass);
    }
}
