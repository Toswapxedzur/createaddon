package com.minecart.createaddon;

import com.minecart.createaddon.config.ModConfigs;
import com.minecart.createaddon.ponder.ModPonder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateAddon.MODID)
public class CreateAddon {
    public static final String MODID = "createaddon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    static {
//        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
        REGISTRATE.defaultCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public CreateAddon(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext context = ModLoadingContext.get();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModDatagen::gatherData);
        REGISTRATE.registerEventListeners(modEventBus);
        ModBlocks.register();
        ModBlockEntities.register();
        ModConfigs.register(context, modContainer);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModPartialModel.register();
            PonderIndex.addPlugin(new ModPonder());
        }
    }

    public static void registerLangEntries() {
        CreateAddon.REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.speed",
                "Rotation Speed: %s RPM"
        );

        CreateAddon.REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.tune",
                "Current Tune: %s at the current speed"
        );

        CreateAddon.REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.frequency",
                "With frequency %s beats per second"
        );

        CreateAddon.REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.volumn",
                "With volumn: "
        );

        CreateAddon.REGISTRATE.addRawLang(
                "createaddon.behaviour.volume",
                "Select volumn"
        );
    }

    public static ResourceLocation modLoc(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
