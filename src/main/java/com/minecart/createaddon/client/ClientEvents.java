package com.minecart.createaddon.client;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.ModPartialModel;
import com.minecart.createaddon.client.renderer.LabwareRenderer;
import com.minecart.createaddon.ponder.ModPonder;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = com.minecart.createaddon.CreateAddon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        PonderIndex.addPlugin(new ModPonder());
    }
}
