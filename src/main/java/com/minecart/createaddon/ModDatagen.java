package com.minecart.createaddon;

import com.minecart.createaddon.data.recipe.CreateAddonRecipeProvider;
import com.minecart.createaddon.ponder.ModPonder;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModDatagen {
    public static void gatherData(GatherDataEvent event){
        BiConsumer<String, String> addLang = (k, v) -> CreateAddon.REGISTRATE.addRawLang(k, v);

        PonderIndex.addPlugin(new ModPonder());
        PonderIndex.getLangAccess().provideLang(CreateAddon.MODID, addLang);

        CreateAddon.registerLangEntries();

        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        if(event.includeServer()){
            CreateAddonRecipeProvider.registerAllProcessing(gen, output, lookup);
        }
    }
}
