package com.minecart.createaddon.data.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateAddonRecipeProvider extends RecipeProvider {
    static final List<ProcessingRecipeGen<?, ?, ?>> GENERATORS = new ArrayList<>();

    public CreateAddonRecipeProvider(PackOutput p_248933_, CompletableFuture<HolderLookup.Provider> p_323846_) {
        super(p_248933_, p_323846_);
    }

    public static void registerAllProcessing(DataGenerator gen, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        GENERATORS.add(new CreateAddonCompressingRecipeGen(output, registries));
        GENERATORS.add(new CreateAddonSieveRecipeGen(output, registries));
        gen.addProvider(true, new CreateAddonSequencedAssemblyRecipeGen(output, registries));

        gen.addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cachedOutput) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(cachedOutput))
                        .toArray(CompletableFuture[]::new));
            }

            @Override
            public String getName() {
                return "Big Press Processing Recipe";
            }
        });
    }
}
