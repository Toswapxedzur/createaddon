package com.minecart.createaddon.recipes.compressing;

import com.minecart.createaddon.recipes.ModRecipes;
import com.simibubi.create.api.data.recipe.DatagenMod;
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
 * 1.20.1 port: extends {@link ProcessingRecipeGen} (no generic — Create 6.0.8 dropped the
 * {@code StandardProcessingRecipeGen<T>} that the 1.21.1 source extended).
 *
 * <p>Constructor takes {@code (PackOutput, String defaultNamespace)} only — the 1.21.1
 * {@code CompletableFuture<HolderLookup.Provider>} parameter doesn't exist at the
 * Create datagen layer in 6.0.8.
 *
 * <p>Static {@code RegisteredObjectsHelper.getKeyOrThrow(item)} (1.21.1 catnip) is replaced
 * with the platform-routed {@link CatnipServices#REGISTRIES} service in 1.20.1 catnip.
 */
public abstract class CompressingRecipeGen extends ProcessingRecipeGen {
    public CompressingRecipeGen(PackOutput output, String defaultNamespace) {
        super(output, defaultNamespace);
    }

    public GeneratedRecipe compress(Supplier<Ingredient> input, NonNullList<ProcessingOutput> result, int time) {
        return create(asResource(CatnipServices.REGISTRIES.getKeyOrThrow(result.get(0).getStack().getItem()).getPath()),
                p -> p.require(input.get()).withItemOutputs(result).duration(time));
    }

    public GeneratedRecipe compressModded(DatagenMod mod, String input, NonNullList<String> result, int time) {
        return create("compat/" + mod.getId() + "/" + result.get(0),
                p -> {
                    for (String entry : result)
                        p.output(mod, entry);
                    p.require(mod, input).duration(time);
                    return p;
                });
    }

    public GeneratedRecipe compress(ItemLike input, ItemLike output, int time) {
        return compress(
                () -> Ingredient.of(input),
                singleOutput(output, 1.0f),
                time
        );
    }

    public GeneratedRecipe compress(TagKey<Item> inputTag, ItemLike output, int time) {
        return compress(
                () -> Ingredient.of(inputTag),
                singleOutput(output, 1.0f),
                time
        );
    }

    public GeneratedRecipe compress(ItemLike input, ItemLike output, float chance, int time) {
        return compress(
                () -> Ingredient.of(input),
                singleOutput(output, chance),
                time
        );
    }

    public GeneratedRecipe compress(ItemLike input, int time, ItemLike... outputs) {
        NonNullList<ProcessingOutput> results = NonNullList.create();
        for (ItemLike item : outputs) {
            results.add(new ProcessingOutput(new ItemStack(item), 1.0f));
        }
        return compress(
                () -> Ingredient.of(input),
                results,
                time
        );
    }

    private NonNullList<ProcessingOutput> singleOutput(ItemLike output, float chance) {
        NonNullList<ProcessingOutput> list = NonNullList.create();
        list.add(new ProcessingOutput(new ItemStack(output), chance));
        return list;
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return ModRecipes.COMPRESSING;
    }
}
