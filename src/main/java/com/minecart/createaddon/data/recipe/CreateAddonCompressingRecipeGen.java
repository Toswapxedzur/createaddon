package com.minecart.createaddon.data.recipe;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.recipes.compressing.CompressingRecipeGen;
import com.simibubi.create.AllItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * 1.20.1 port: constructor signature drops the {@code CompletableFuture<HolderLookup.Provider>}
 * (Create 6.0.8's {@code ProcessingRecipeGen} doesn't accept it).
 */
public class CreateAddonCompressingRecipeGen extends CompressingRecipeGen {
    @SuppressWarnings("unused")
    public final GeneratedRecipe GOLDEN_SHEET = compress(Items.GOLD_INGOT, AllItems.GOLDEN_SHEET, 400);

    public CreateAddonCompressingRecipeGen(PackOutput output) {
        super(output, CreateAddon.MODID);
    }
}
