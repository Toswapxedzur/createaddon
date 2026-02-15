package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<NoteblockEncasedShaftBlockEntity> noteblock_ENCASED_SHAFT = REGISTRATE
            .blockEntity("noteblock_encased_shaft", NoteblockEncasedShaftBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.NOTEBLOCK_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<NoteblockEncasedCogwheelBlockEntity> noteblock_ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("noteblock_encased_cogwheel", NoteblockEncasedCogwheelBlockEntity::new)
            .visual(() -> EncasedCogVisual::small, false)
            .validBlocks(ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL)
            .renderer(() -> EncasedCogRenderer::small)
            .register();

    public static final BlockEntityEntry<CalibratedNoteBlockEncasedShaftBlockEntity> CALIBRATED_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .blockEntity("calibrated_noteblock_encased_shaft", CalibratedNoteBlockEncasedShaftBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.CALIBRATED_NOTEBLOCK_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static void register() {
    }
}
