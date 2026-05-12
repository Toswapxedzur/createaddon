package com.minecart.createaddon;

import com.minecart.createaddon.block_entities.extrusion.ExtrusionDieDecompositionCache;
import com.minecart.createaddon.config.ModConfigs;
import com.minecart.createaddon.recipes.ModRecipes;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateAddon.MODID)
public class CreateAddon {
    public static final String MODID = "createaddon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    static {
        REGISTRATE.defaultCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public CreateAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext context = ModLoadingContext.get();

        modEventBus.addListener(this::commonSetup);
        REGISTRATE.registerEventListeners(modEventBus);

        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModRecipes.register(modEventBus);
        ModConfigs.register(context);

        MinecraftForge.EVENT_BUS.register(this);

        registerLangEntries();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    /**
     * Forge's equivalent of the 1.21.1 {@code AddReloadListenerEvent}-via-NeoForge wiring.
     * Invalidates Extrusion Die decomposition lookups on /reload so updated recipes are picked up.
     */
    @SubscribeEvent
    public void onReloadListeners(AddReloadListenerEvent event) {
        ExtrusionDieDecompositionCache.invalidateAfterResourceReload();
    }

    public static void registerLangEntries() {
        REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.speed",
                "Rotation Speed: %s RPM"
        );
        REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.tune",
                "Current Tune: %s at the current speed"
        );
        REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.frequency",
                "With frequency %s beats per second"
        );
        REGISTRATE.addRawLang(
                "create.createaddon.tooltip.kinetic_noteblock.volumn",
                "With volumn: "
        );
        REGISTRATE.addRawLang(
                "createaddon.behaviour.volume",
                "Select volumn"
        );
    }

    public static ResourceLocation modLoc(String path) {
        return new ResourceLocation(MODID, path);
    }
}
