package com.minecart.createaddon;

import com.minecart.createaddon.data.recipe.CreateAddonRecipeProvider;
import com.minecart.createaddon.ponder.ModPonder;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.function.BiConsumer;

/**
 * 1.20.1 Forge port of the 1.21.1 {@code ModDatagen}.
 * <ul>
 *   <li>Uses {@code net.minecraftforge.data.event.GatherDataEvent} (not NeoForge's
 *       {@code net.neoforged.neoforge.data.event.GatherDataEvent}).</li>
 *   <li>Doesn't pass {@code CompletableFuture<HolderLookup.Provider>} through to recipe gens
 *       — Create 6.0.8's {@code ProcessingRecipeGen}/{@code SequencedAssemblyRecipeGen}
 *       constructors don't accept it.</li>
 *   <li>Lang entries are fed into Registrate via {@link CreateAddon#registerLangEntries()}
 *       and Ponder's {@code provideLang}, then Registrate's {@code RegistrateLangProvider}
 *       (auto-registered by {@code REGISTRATE.registerEventListeners}) emits the
 *       {@code en_us.json} during datagen.</li>
 * </ul>
 */
public final class ModDatagen {
    private ModDatagen() {
    }

    public static void gatherData(GatherDataEvent event) {
        BiConsumer<String, String> addLang = CreateAddon.REGISTRATE::addRawLang;

        PonderIndex.addPlugin(new ModPonder());
        PonderIndex.getLangAccess().provideLang(CreateAddon.MODID, addLang);

        CreateAddon.registerLangEntries();

        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();

        if (event.includeServer()) {
            CreateAddonRecipeProvider.registerAllProcessing(gen, output);
        }
    }
}
