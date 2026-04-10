package com.minecart.createaddon.data.recipe;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModItems;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public final class CreateAddonSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {
    /** Coal block is cycled through alternating depot presses and big mechanical presses, then yields diamond. */
    public final GeneratedRecipe DIAMOND_SYNTHESIS = create("diamond_synthesis", b -> b
            .require(Items.COAL_BLOCK)
            .transitionTo(ModItems.INCOMPLETE_DIAMOND.get())
            .addOutput(Items.DIAMOND, 1)
            .loops(10)
            .addStep(PressingRecipe::new, rb -> rb)
            .addStep(CompressingRecipe::new, rb -> rb.duration(500))
            .addStep(PressingRecipe::new, rb -> rb)
            .addStep(CompressingRecipe::new, rb -> rb.duration(500)));

    public CreateAddonSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateAddon.MODID);
    }
}
