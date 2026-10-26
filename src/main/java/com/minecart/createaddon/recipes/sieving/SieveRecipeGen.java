package com.minecart.createaddon.recipes.sieving;

import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class SieveRecipeGen extends StandardProcessingRecipeGen<SieveRecipe> {
    public SieveRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
        super(output, registries, defaultNamespace);
    }

    /** Core builder: input + arbitrary output list (each entry has its own chance). */
    public GeneratedRecipe sieve(String name, Supplier<Ingredient> input, NonNullList<ProcessingOutput> results, int duration) {
        return create(asResource(name),
                p -> p.require(input.get()).withItemOutputs(results).duration(duration));
    }

    /** Auto-name based on first output's item id. */
    public GeneratedRecipe sieve(Supplier<Ingredient> input, NonNullList<ProcessingOutput> results, int duration) {
        String name = RegisteredObjectsHelper.getKeyOrThrow(results.get(0).getStack().getItem()).getPath();
        return sieve(name, input, results, duration);
    }

    public GeneratedRecipe sieve(ItemLike input, int duration, ProcessingOutput... outputs) {
        NonNullList<ProcessingOutput> list = NonNullList.create();
        for (ProcessingOutput o : outputs) list.add(o);
        return sieve(() -> Ingredient.of(input), list, duration);
    }

    public GeneratedRecipe sieve(TagKey<Item> input, int duration, ProcessingOutput... outputs) {
        NonNullList<ProcessingOutput> list = NonNullList.create();
        for (ProcessingOutput o : outputs) list.add(o);
        return sieve(() -> Ingredient.of(input), list, duration);
    }

    /** Convenience: 1:1 with a chance. */
    public GeneratedRecipe sieve(ItemLike input, ItemLike output, float chance, int duration) {
        return sieve(input, duration, out(output, 1, chance));
    }

    public static ProcessingOutput out(ItemLike item, int count, float chance) {
        return new ProcessingOutput(new ItemStack(item, count), chance);
    }

    public static ProcessingOutput out(ItemLike item, float chance) {
        return out(item, 1, chance);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return ModRecipes.SIEVING;
    }
}
