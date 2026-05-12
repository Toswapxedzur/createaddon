package com.minecart.createaddon.client;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.ModPartialModel;
import com.minecart.createaddon.client.renderer.LabwareRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = com.minecart.createaddon.CreateAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BEAKER.get(), LabwareRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MEASURING_CYLINDER.get(), LabwareRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModPartialModel.register();
        // TODO: Wire ponder once ModPonder is ported for 1.20.1
        // PonderIndex.addPlugin(new ModPonder());
    }
}
