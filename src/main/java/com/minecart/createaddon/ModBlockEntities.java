package com.minecart.createaddon;

import com.minecart.createaddon.block_entities.ACNESBlockEntity;
import com.minecart.createaddon.block_entities.AndesiteNoteblockEncasedBlockEntity;
import com.minecart.createaddon.block_entities.BCNESBlockEntity;
import com.minecart.createaddon.block_entities.BrassNoteblockEncasedBlockEntity;
import com.minecart.createaddon.block_entities.KineticSculkSensorBlockEntity;
import com.minecart.createaddon.block_entities.bigPress.BigPressBlockEntity;
import com.minecart.createaddon.block_entities.bigPress.BigPressRenderer;
import com.minecart.createaddon.block_entities.bigPress.BigPressVisual;
import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieBlockEntity;
import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieRenderer;
import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieVisual;
import com.minecart.createaddon.block_entities.labware.BeakerBlockEntity;
import com.minecart.createaddon.block_entities.labware.MeasuringCylinderBlockEntity;
import com.minecart.createaddon.block_entities.sieve.MechanicalSieveBlockEntity;
import com.minecart.createaddon.block_entities.sieve.MechanicalSieveRenderer;
import com.minecart.createaddon.block_entities.sieve.MechanicalSieveVisual;
import com.minecart.createaddon.client.renderer.CogRenderer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
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
    public static final BlockEntityEntry<AndesiteNoteblockEncasedBlockEntity> ANDESITE_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .blockEntity("noteblock_encased_shaft", AndesiteNoteblockEncasedBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.NOTEBLOCK_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<AndesiteNoteblockEncasedBlockEntity> ANDESITE_NOTEBLOCK_ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("noteblock_encased_cogwheel", AndesiteNoteblockEncasedBlockEntity::new)
            .visual(() -> EncasedCogVisual::small, false)
            .validBlocks(ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL)
            .renderer(() -> CogRenderer::new)
            .register();

    public static final BlockEntityEntry<ACNESBlockEntity> ANDESITE_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .blockEntity("andesite_calibrated_noteblock_encased_shaft", ACNESBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.ANDESITE_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<BCNESBlockEntity> BRASS_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .blockEntity("brass_calibrated_noteblock_encased_shaft", BCNESBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(ModBlocks.BRASS_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<BrassNoteblockEncasedBlockEntity> BRASS_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .blockEntity("brass_noteblock_encased_shaft", BrassNoteblockEncasedBlockEntity::new)
            .visual(() -> ShaftVisual::new)
            .validBlocks(ModBlocks.BRASS_NOTEBLOCK_ENCASED_SHAFT)
            // Use the standard renderer (Handles spinning shaft + Scroll Value overlay automatically)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<BrassNoteblockEncasedBlockEntity> BRASS_NOTEBLOCK_ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("brass_noteblock_encased_cogwheel", BrassNoteblockEncasedBlockEntity::new)
            .visual(() -> EncasedCogVisual::small)
            .validBlocks(ModBlocks.BRASS_NOTEBLOCK_ENCASED_COGWHEEL)
            .register();

    public static final BlockEntityEntry<KineticSculkSensorBlockEntity> KINETIC_SCULK_SENSOR = REGISTRATE
            .blockEntity("kinetic_sculk_sensor", KineticSculkSensorBlockEntity::new)
            .visual(() -> (ctx, be, tick) -> new SingleAxisRotatingVisual<KineticSculkSensorBlockEntity>(ctx, be, tick, Models.partial(ModPartialModel.QUATERED_SHAFT)), false)
            .validBlocks(ModBlocks.KINETIC_SCULK_SENSOR)
            .renderer(() -> ctx -> new KineticBlockEntityRenderer<KineticSculkSensorBlockEntity>(ctx) {
                @Override
                protected SuperByteBuffer getRotatedModel(KineticSculkSensorBlockEntity be, BlockState state) {
                    return CachedBuffers.partial(ModPartialModel.QUATERED_SHAFT,
                            AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
                }
            })
            .register();

    public static final BlockEntityEntry<BigPressBlockEntity> BIGPRESS = REGISTRATE
            .blockEntity("big_mechanical_press", BigPressBlockEntity::new)
            .visual(() -> BigPressVisual::new)
            .validBlock(ModBlocks.BIGPRESS)
            .renderer(() -> BigPressRenderer::new)
            .register();

    public static final BlockEntityEntry<MechanicalSieveBlockEntity> MECHANICAL_SIEVE = REGISTRATE
            .blockEntity("mechanical_sieve", MechanicalSieveBlockEntity::new)
            .visual(() -> MechanicalSieveVisual::new, true)
            .validBlocks(ModBlocks.MECHANICAL_SIEVE)
            .renderer(() -> MechanicalSieveRenderer::new)
            .register();

    public static final BlockEntityEntry<ExtrusionDieBlockEntity> EXTRUSION_DIE = REGISTRATE
            .blockEntity("extrusion_die", ExtrusionDieBlockEntity::new)
            .visual(() -> ExtrusionDieVisual::new, false)
            .validBlocks(ModBlocks.EXTRUSION_DIE)
            .renderer(() -> ExtrusionDieRenderer::new)
            .register();

    public static final BlockEntityEntry<BeakerBlockEntity> BEAKER = REGISTRATE
            .blockEntity("beaker", BeakerBlockEntity::new)
            .validBlocks(ModBlocks.BEAKER)
            .register();

    public static final BlockEntityEntry<MeasuringCylinderBlockEntity> MEASURING_CYLINDER = REGISTRATE
            .blockEntity("measuring_cylinder", MeasuringCylinderBlockEntity::new)
            .validBlocks(ModBlocks.MEASURING_CYLINDER)
            .register();

    public static void register() {
    }
}
