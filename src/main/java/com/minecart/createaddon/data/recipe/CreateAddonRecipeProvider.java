package com.minecart.createaddon.data.recipe;

import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

/**
 * Aggregates this addon's processing & sequenced-assembly recipe generators and registers
 * them with the supplied {@link DataGenerator}. 1.20.1 differences from the 1.21.1 source:
 * <ul>
 *   <li>Generators are added directly via {@code gen.addProvider(true, instance)} — no
 *       {@code CompletableFuture<HolderLookup.Provider>} threading is needed because
 *       {@link BaseRecipeProvider} owns its own recipe list and uses the standard
 *       {@code RecipeProvider#run(...)} pipeline.</li>
 *   <li>No bespoke aggregator {@code DataProvider} (the 1.21.1 version manually fanned-out
 *       to {@code CompletableFuture.allOf}); each gen is now a normal data provider.</li>
 * </ul>
 */
public final class CreateAddonRecipeProvider {
    private CreateAddonRecipeProvider() {
    }

    public static void registerAllProcessing(DataGenerator gen, PackOutput output) {
        gen.addProvider(true, new CreateAddonCompressingRecipeGen(output));
        gen.addProvider(true, new CreateAddonSieveRecipeGen(output));
        gen.addProvider(true, new CreateAddonSequencedAssemblyRecipeGen(output));
    }
}
