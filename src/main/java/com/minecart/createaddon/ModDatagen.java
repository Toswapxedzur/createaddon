package com.minecart.createaddon;

import com.minecart.createaddon.ponder.ModPonder;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.function.BiConsumer;

public class ModDatagen {
    public static void gatherData(GatherDataEvent event){
        BiConsumer<String, String> addLang = (k, v) -> CreateAddon.REGISTRATE.addRawLang(k, v);

        PonderIndex.addPlugin(new ModPonder());
        PonderIndex.getLangAccess().provideLang(CreateAddon.MODID, addLang);

        CreateAddon.registerLangEntries();
    }
}
