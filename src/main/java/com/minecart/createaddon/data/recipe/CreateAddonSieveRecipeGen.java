package com.minecart.createaddon.data.recipe;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.recipes.sieving.SieveRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CreateAddonSieveRecipeGen extends SieveRecipeGen {

    public final GeneratedRecipe
            GRAVEL = sieve(Items.GRAVEL, 120,
                    out(Items.FLINT, 0.50f),
                    out(Items.IRON_NUGGET, 0.20f),
                    out(Items.COAL, 0.10f)),

            SAND = sieve(Items.SAND, 100,
                    out(Items.GOLD_NUGGET, 0.05f),
                    out(Items.GLOWSTONE_DUST, 0.10f)),

            DIRT = sieve(Items.DIRT, 80,
                    out(Items.WHEAT_SEEDS, 0.40f),
                    out(Items.STICK, 0.20f),
                    out(Items.BONE_MEAL, 0.10f)),

            SOUL_SAND = sieve(Items.SOUL_SAND, 160,
                    out(Items.QUARTZ, 0.25f),
                    out(Items.NETHER_WART, 0.10f),
                    out(Items.GOLD_NUGGET, 0.05f));

    public CreateAddonSieveRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateAddon.MODID);
    }
}
