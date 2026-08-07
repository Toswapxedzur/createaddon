package com.minecart.createaddon.client;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.ModPartialModel;
import com.minecart.createaddon.client.renderer.LabwareRenderer;
import com.minecart.createaddon.ponder.ModPonder;
import net.createmod.ponder.foundation.PonderIndex;
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
        PonderIndex.addPlugin(new ModPonder());
    }
}
