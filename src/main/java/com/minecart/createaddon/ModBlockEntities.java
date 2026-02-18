package com.minecart.createaddon;

import com.minecart.createaddon.block_entities.CalibratedNoteBlockEncasedShaftBlockEntity;
import com.minecart.createaddon.block_entities.KineticSculkSensorBlockEntity;
import com.minecart.createaddon.block_entities.NoteblockEncasedCogwheelBlockEntity;
import com.minecart.createaddon.block_entities.NoteblockEncasedShaftBlockEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

    public static final BlockEntityEntry<KineticSculkSensorBlockEntity> KINETIC_SCULK_SENSOR = REGISTRATE
            .blockEntity("kinetic_sculk_sensor", KineticSculkSensorBlockEntity::new)
            .visual(() -> (ctx, be, tick) -> new SingleAxisRotatingVisual<KineticSculkSensorBlockEntity>(ctx, be, tick, Models.partial(ModPartialModel.QUATERED_SHAFT)), false)
            .validBlocks(ModBlocks.KINETIC_SCULK_SENSOR)
            .renderer(() -> ctx -> new KineticBlockEntityRenderer<KineticSculkSensorBlockEntity>(ctx){
                @Override
                protected SuperByteBuffer getRotatedModel(KineticSculkSensorBlockEntity be, BlockState state) {
                    return CachedBuffers.partial(ModPartialModel.QUATERED_SHAFT, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
                }
            })
            .register();

    public static void register() {
    }
}
