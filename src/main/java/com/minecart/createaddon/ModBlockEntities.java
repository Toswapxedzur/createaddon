package com.minecart.createaddon;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<JukeboxEncasedShaftBlockEntity> JUKEBOX_ENCASED_SHAFT = REGISTRATE
            .blockEntity("jukebox_encased_shaft", JukeboxEncasedShaftBlockEntity::new)
            .validBlocks(ModBlocks.JUKEBOX_ENCASED_SHAFT)
            .register();

    public static void register() {
    }
}
