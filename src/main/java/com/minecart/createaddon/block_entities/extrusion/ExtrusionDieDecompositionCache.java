package com.minecart.createaddon.block_entities.extrusion;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExtrusionDieDecompositionCache {
    private static volatile Map<Item, ItemStack> DECOMPOSITIONS = Map.of();
    private static volatile boolean loaded = false;

    private ExtrusionDieDecompositionCache() {}

    public static void invalidateAfterResourceReload() {
        DECOMPOSITIONS = Map.of();
        loaded = false;
    }

    private static boolean pressCanCompressSafe(CraftingRecipe recipe) {
        try {
            return MechanicalPressBlockEntity.canCompress(recipe);
        } catch (IllegalStateException ignored) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            return (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.matchAllIngredients(ingredients);
        }
    }

    private static void rebuild(RecipeManager recipeManager, RegistryAccess registries) {
        Map<Item, ItemStack> next = new HashMap<>();
        // 1.20.1: getAllRecipesFor returns List<R> directly (no RecipeHolder wrapper).
        for (CraftingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (recipe instanceof MechanicalCraftingRecipe)
                continue;
            if (AllRecipeTypes.shouldIgnoreInAutomation(recipe))
                continue;
            if (!pressCanCompressSafe(recipe))
                continue;

            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty() || result.getCount() != 1)
                continue;

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty())
                continue;
            ItemStack[] stacks = ingredients.get(0).getItems();
            if (stacks.length == 0)
                continue;

            Item key = result.getItem();
            ItemStack value = stacks[0].copyWithCount(ingredients.size());
            next.putIfAbsent(key, value);
        }
        DECOMPOSITIONS = Map.copyOf(next);
    }

    public static Optional<ItemStack> lookup(ServerLevel level, BlockState state) {
        if (!loaded) {
            synchronized (ExtrusionDieDecompositionCache.class) {
                if (!loaded) {
                    rebuild(level.getRecipeManager(), level.registryAccess());
                    loaded = true;
                }
            }
        }

        ItemStack blockAsItem = new ItemStack(state.getBlock().asItem());
        if (blockAsItem.isEmpty())
            return Optional.empty();

        ItemStack cached = DECOMPOSITIONS.get(blockAsItem.getItem());
        if (cached == null)
            return Optional.empty();
        return Optional.of(cached.copy());
    }
}
