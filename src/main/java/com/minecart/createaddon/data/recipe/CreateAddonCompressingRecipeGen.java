package com.minecart.createaddon.data.recipe;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.recipes.compressing.CompressingRecipeGen;
import com.simibubi.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CreateAddonCompressingRecipeGen extends CompressingRecipeGen {
    public final GeneratedRecipe GOLDEN_SHEET = compress(Items.GOLD_INGOT, AllItems.GOLDEN_SHEET, 400);

    public CreateAddonCompressingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateAddon.MODID);
    }
}
