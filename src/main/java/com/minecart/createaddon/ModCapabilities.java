package com.minecart.createaddon;

import com.minecart.createaddon.fluid.LabwareItemFluidHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = CreateAddon.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BEAKER.get(), (be, ctx) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MEASURING_CYLINDER.get(), (be, ctx) -> be.getFluidHandler());

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> {
            if (stack.is(ModBlocks.BEAKER.asItem())) {
                return new LabwareItemFluidHandler(stack, 250);
            }
            if (stack.is(ModBlocks.MEASURING_CYLINDER.asItem())) {
                return new LabwareItemFluidHandler(stack, 50);
            }
            return null;
        }, ModBlocks.BEAKER, ModBlocks.MEASURING_CYLINDER);
    }

    private ModCapabilities() {
    }
}
