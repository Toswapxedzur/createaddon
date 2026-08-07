package com.minecart.createaddon.data.recipe;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModItems;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * 1.20.1 port: constructor signature drops the {@code CompletableFuture<HolderLookup.Provider>}
 * (Create 6.0.8's {@code SequencedAssemblyRecipeGen} doesn't accept it).
 *
 * <p>The deployer step from the 1.21.1 source is left commented out for parity (was already
 * commented in 1.21.1 because it required {@code AllItems.ZINC_INGOT}).
 */
@SuppressWarnings("unused")
public final class CreateAddonSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {
    /** Coal block is cycled through alternating presses, transitioning into incomplete diamond, finally yielding diamond. */
    public final GeneratedRecipe DIAMOND_SYNTHESIS = create("diamond_synthesis", b -> b
            .require(Items.COAL_BLOCK)
            .transitionTo(ModItems.INCOMPLETE_DIAMOND.get())
            .addOutput(Items.DIAMOND, 1)
            .loops(10)
            .addStep(PressingRecipe::new, rb -> rb)
//            .addStep(DeployerApplicationRecipe::new, dp -> dp.require(AllItems.ZINC_INGOT))
            .addStep(CompressingRecipe::new, rb -> rb.duration(500))
            .addStep(PressingRecipe::new, rb -> rb)
            .addStep(CompressingRecipe::new, rb -> rb.duration(500)));

    public CreateAddonSequencedAssemblyRecipeGen(PackOutput output) {
        super(output, CreateAddon.MODID);
    }
}
