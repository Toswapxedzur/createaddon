package com.minecart.createaddon;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.CreativeModeTabs;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModItems {

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_DIAMOND = REGISTRATE
            .item("incomplete_diamond", SequencedAssemblyItem::new)
            .lang("Incomplete Diamond")
            .removeTab(CreativeModeTabs.BUILDING_BLOCKS)
            .model(AssetLookup.existingItemModel())
            .register();

    public static void register() {
    }
}
