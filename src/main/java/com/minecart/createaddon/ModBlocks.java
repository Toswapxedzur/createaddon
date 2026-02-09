package com.minecart.createaddon;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.minecart.createaddon.CreateAddon.REGISTRATE;

public class ModBlocks {
    public static final BlockEntry<JukeboxEncasedShaftBlock> JUKEBOX_ENCASED_SHAFT = REGISTRATE
            .block("jukebox_encased_shaft", JukeboxEncasedShaftBlock::new)
            .initialProperties(() -> Blocks.JUKEBOX)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.noOcclusion())
//            .blockstate((c, p) -> p.simpleBlock(c.get()))
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(TagGen.pickaxeOnly())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .lang("Jukebox Encased Shaft")
            .tag(AllTags.AllBlockTags.CASING.tag)
            .loot((loot, block) -> loot.dropOther(block, Items.JUKEBOX))
            .item()
            .tab(CreativeModeTabs.BUILDING_BLOCKS)
            .removeTab(CreativeModeTabs.COMBAT)
//            .model(AssetLookup::customItemModel).build()
            .transform(ModelGen.customItemModel())
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .requires(Items.JUKEBOX).requires(AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_shaft", RegistrateRecipeProvider.has(AllBlocks.SHAFT.asItem()))
                        .save(provider, CreateAddon.modLoc("jukebox_encased_shaft_using_shafts"));
            })
            .register();

    public static void register() {
    }
}
