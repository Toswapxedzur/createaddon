package com.minecart.createaddon.recipes.sieving;

import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * 1.20.1 port: see {@link com.minecart.createaddon.recipes.compressing.CompressingRecipeGen}
 * for the API differences against 1.21.1 — {@code StandardProcessingRecipeGen<T>} is replaced
 * with non-generic {@link ProcessingRecipeGen}, the constructor drops the registries lookup,
 * and {@code RegisteredObjectsHelper} static calls become {@link CatnipServices#REGISTRIES}.
 */
public abstract class SieveRecipeGen extends ProcessingRecipeGen {
    public SieveRecipeGen(PackOutput output, String defaultNamespace) {
        super(output, defaultNamespace);
    }

    public GeneratedRecipe sieve(String name, Supplier<Ingredient> input, NonNullList<ProcessingOutput> results, int duration) {
        return create(asResource(name),
                p -> p.require(input.get()).withItemOutputs(results).duration(duration));
    }

    public GeneratedRecipe sieve(Supplier<Ingredient> input, NonNullList<ProcessingOutput> results, int duration) {
        String name = CatnipServices.REGISTRIES.getKeyOrThrow(results.get(0).getStack().getItem()).getPath();
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
