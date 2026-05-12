package com.minecart.createaddon;

import com.minecart.createaddon.block.KineticSculkSensorBlock;
import com.minecart.createaddon.block.andesite.ACNESBlock;
import com.minecart.createaddon.block.andesite.AndesiteNoteblockEncasedCogwheelBlock;
import com.minecart.createaddon.block.andesite.AndesiteNoteblockEncasedShaftBlock;
import com.minecart.createaddon.block.brass.BCNESBlock;
import com.minecart.createaddon.block.brass.BrassNoteblockEncasedCogwheelBlock;
import com.minecart.createaddon.block.brass.BrassNoteblockEncasedShaftBlock;
import com.minecart.createaddon.block.labware.BeakerBlock;
import com.minecart.createaddon.block.labware.MeasuringCylinderBlock;
import com.minecart.createaddon.block_entities.bigPress.BigPressBlock;
import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieBlock;
import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieMovementBehaviour;
import com.minecart.createaddon.block_entities.sieve.MechanicalSieveBlock;
import com.minecart.createaddon.config.ModStress;
import com.minecart.createaddon.item.LabwareBlockItem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.material.MapColor;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModBlocks {
    public static final BlockEntry<AndesiteNoteblockEncasedShaftBlock> NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .block("noteblock_encased_shaft", AndesiteNoteblockEncasedShaftBlock::new)
            .initialProperties(() -> Blocks.NOTE_BLOCK)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(TagGen.pickaxeOnly())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .lang("Noteblock Encased Shaft")
            .tag(AllTags.AllBlockTags.CASING.tag)
            .transform(ModStress.setImpact(16))
            .item()
            .removeTab(CreativeModeTabs.COMBAT)
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .requires(Items.NOTE_BLOCK).requires(AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_shaft", RegistrateRecipeProvider.has(AllBlocks.SHAFT.asItem()))
                        .save(provider, CreateAddon.modLoc("noteblock_encased_shaft_using_shafts"));
            })
            .register();

    public static final BlockEntry<AndesiteNoteblockEncasedCogwheelBlock> NOTEBLOCK_ENCASED_COGWHEEL = REGISTRATE
            .block("noteblock_encased_cogwheel", AndesiteNoteblockEncasedCogwheelBlock::new)
            .initialProperties(() -> Blocks.NOTE_BLOCK)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(TagGen.pickaxeOnly())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .tag(AllTags.AllBlockTags.CASING.tag)
            .transform(ModStress.setImpact(16))
            .lang("Noteblock Encased Cogwheel")
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .requires(Items.NOTE_BLOCK)
                        .requires(AllBlocks.COGWHEEL.asItem())
                        .unlockedBy("has_cogwheel", RegistrateRecipeProvider.has(AllBlocks.COGWHEEL.asItem()))
                        .save(provider, CreateAddon.modLoc("noteblock_encased_cogwheel_using_cogs"));
            })
            .register();

    public static final BlockEntry<ACNESBlock> ANDESITE_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .block("andesite_calibrated_noteblock_encased_shaft", ACNESBlock::new)
            .initialProperties(() -> Blocks.NOTE_BLOCK)
            .properties(p -> p.mapColor(MapColor.WOOD))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(TagGen.pickaxeOnly())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(AllTags.AllBlockTags.CASING.tag)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .transform(ModStress.setImpact(16))
            .lang("Andesite Calibrated Noteblock Encased Shaft")
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .requires(Items.AMETHYST_SHARD).requires(AllItems.ANDESITE_ALLOY.asItem())
                        .requires(Blocks.NOTE_BLOCK).requires(AllBlocks.SHAFT)
                        .requires(AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_shaft", RegistrateRecipeProvider.has(AllBlocks.SHAFT.asItem()))
                        .save(provider, CreateAddon.modLoc("andesite_calibrated_noteblock_encased_shaft"));
            })
            .register();

    public static final BlockEntry<BCNESBlock> BRASS_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .block("brass_calibrated_noteblock_encased_shaft", BCNESBlock::new)
            .initialProperties(() -> Blocks.NOTE_BLOCK)
            .properties(p -> p.mapColor(MapColor.WOOD))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(TagGen.pickaxeOnly())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(AllTags.AllBlockTags.CASING.tag)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .transform(ModStress.setImpact(16))
            .lang("Brass Calibrated Noteblock Encased Shaft")
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .requires(Items.AMETHYST_SHARD).requires(AllItems.BRASS_INGOT.asItem())
                        .requires(Blocks.NOTE_BLOCK).requires(AllBlocks.SHAFT)
                        .requires(AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_shaft", RegistrateRecipeProvider.has(AllBlocks.SHAFT.asItem()))
                        .save(provider, CreateAddon.modLoc("brass_calibrated_noteblock_encased_shaft"));
            })
            .register();

    public static final BlockEntry<BrassNoteblockEncasedShaftBlock> BRASS_NOTEBLOCK_ENCASED_SHAFT = REGISTRATE
            .block("brass_noteblock_encased_shaft", BrassNoteblockEncasedShaftBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(AllTags.AllBlockTags.CASING.tag)
            .lang("Brass Noteblock Encased Shaft")
            .transform(ModStress.setImpact(16))
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ctx.get())
                        .requires(Items.NOTE_BLOCK)
                        .requires(AllItems.BRASS_INGOT)
                        .requires(AllBlocks.SHAFT)
                        .unlockedBy("has_brass_ingot", RegistrateRecipeProvider.has(AllBlocks.BRASS_CASING))
                        .save(provider, CreateAddon.modLoc("crafting/brass_noteblock_encased_shaft"));
            })
            .register();

    public static final BlockEntry<BrassNoteblockEncasedCogwheelBlock> BRASS_NOTEBLOCK_ENCASED_COGWHEEL = REGISTRATE
            .block("brass_noteblock_encased_cogwheel", BrassNoteblockEncasedCogwheelBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(AllTags.AllBlockTags.CASING.tag)
            .transform(ModStress.setImpact(16))
            .lang("Brass Noteblock Encased Cogwheel")
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ctx.get())
                        .requires(Items.NOTE_BLOCK)
                        .requires(AllItems.BRASS_INGOT)
                        .requires(AllBlocks.COGWHEEL)
                        .unlockedBy("has_brass_ingot", RegistrateRecipeProvider.has(AllBlocks.BRASS_CASING))
                        .save(provider, CreateAddon.modLoc("crafting/brass_noteblock_encased_cogwheel"));
            })
            .register();

    public static final BlockEntry<KineticSculkSensorBlock> KINETIC_SCULK_SENSOR = REGISTRATE
            .block("kinetic_sculk_sensor", KineticSculkSensorBlock::new)
            .initialProperties(() -> Blocks.SCULK_SENSOR)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.lightLevel(state -> state.getValue(KineticSculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE ? 5 : 0))
            .blockstate((c, p) -> {})
            .transform(TagGen.pickaxeOnly())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(ModStress.setCapacity(8))
            .lang("Kinetic Sculk Sensor")
            .item()
            .tab(CreativeModeTabs.REDSTONE_BLOCKS)
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ctx.get())
                        .requires(Blocks.SCULK_SENSOR)
                        .requires(AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_sculk_sensor", RegistrateRecipeProvider.has(Blocks.SCULK_SENSOR))
                        .save(provider, CreateAddon.modLoc("kinetic_sculk_sensor"));
            })
            .register();

    public static final BlockEntry<BigPressBlock> BIGPRESS = REGISTRATE
            .block("big_mechanical_press", BigPressBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(properties -> properties.noOcclusion())
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .transform(ModStress.setImpact(16.0))
            .transform(TagGen.axeOrPickaxe())
            .item(AssemblyOperatorBlockItem::new)
            .model(AssetLookup::customItemModel).build()
            .register();

    public static final BlockEntry<MechanicalSieveBlock> MECHANICAL_SIEVE = REGISTRATE
            .block("mechanical_sieve", MechanicalSieveBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> {})
            .transform(ModStress.setImpact(4.0))
            .transform(TagGen.pickaxeOnly())
            .lang("Mechanical Sieve")
            .item()
            .model(AssetLookup::customItemModel).build()
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
                        .pattern("SAS")
                        .pattern("HCH")
                        .pattern("SAS")
                        .define('S', Items.STRING)
                        .define('A', AllItems.ANDESITE_ALLOY)
                        .define('H', AllBlocks.SHAFT)
                        .define('C', AllBlocks.ANDESITE_CASING)
                        .unlockedBy("has_andesite_casing", RegistrateRecipeProvider.has(AllBlocks.ANDESITE_CASING))
                        .save(provider, CreateAddon.modLoc("crafting/mechanical_sieve"));
            })
            .register();

    public static final BlockEntry<ExtrusionDieBlock> EXTRUSION_DIE = REGISTRATE
            .block("extrusion_die", ExtrusionDieBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(properties -> properties.noOcclusion())
            .blockstate((c, p) -> {})
            .transform(ModStress.setImpact(16.0))
            .transform(TagGen.pickaxeOnly())
            .onRegister(MovementBehaviour.movementBehaviour(new ExtrusionDieMovementBehaviour()))
            .lang("Extrusion Die")
            .item()
            .model(AssetLookup::customItemModel).build()
            .register();

    public static final BlockEntry<BeakerBlock> BEAKER = REGISTRATE
            .block("beaker", BeakerBlock::new)
            .initialProperties(() -> Blocks.GLASS)
            .properties(p -> p.strength(0.3f))
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> {})
            .lang("Beaker")
            .item(LabwareBlockItem::new)
            .properties(p -> p.stacksTo(1))
            .model(AssetLookup::customItemModel).build()
            .register();

    public static final BlockEntry<MeasuringCylinderBlock> MEASURING_CYLINDER = REGISTRATE
            .block("measuring_cylinder", MeasuringCylinderBlock::new)
            .initialProperties(() -> Blocks.GLASS)
            .properties(p -> p.strength(0.3f))
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> {})
            .lang("Measuring Cylinder")
            .item(LabwareBlockItem::new)
            .properties(p -> p.stacksTo(1))
            .model(AssetLookup::customItemModel).build()
            .register();

    public static void register() {
    }
}
