package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<JukeboxEncasedShaftBlockEntity> JUKEBOX_ENCASED_SHAFT = REGISTRATE
            .blockEntity("jukebox_encased_shaft", JukeboxEncasedShaftBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.JUKEBOX_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static void register() {
    }
}
