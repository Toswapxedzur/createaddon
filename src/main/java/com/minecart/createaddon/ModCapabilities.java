package com.minecart.createaddon;

import com.minecart.createaddon.fluid.LabwareItemFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.20.1 Forge port. Block-entity fluid capabilities are exposed via the BE's own
 * {@code getCapability} override (see {@link com.minecart.createaddon.block_entities.labware.LabwareBlockEntity}).
 * Item-stack capabilities are attached here via {@link AttachCapabilitiesEvent}.
 */
@Mod.EventBusSubscriber(modid = CreateAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.is(ModBlocks.BEAKER.asItem())) {
            event.addCapability(new ResourceLocation(CreateAddon.MODID, "labware_fluid"),
                    new LabwareItemCapabilityProvider(stack, 250));
        } else if (stack.is(ModBlocks.MEASURING_CYLINDER.asItem())) {
            event.addCapability(new ResourceLocation(CreateAddon.MODID, "labware_fluid"),
                    new LabwareItemCapabilityProvider(stack, 50));
        }
    }

    private static final class LabwareItemCapabilityProvider implements ICapabilityProvider {
        private final LazyOptional<IFluidHandlerItem> opt;

        LabwareItemCapabilityProvider(ItemStack stack, int capacityMb) {
            this.opt = LazyOptional.of(() -> new LabwareItemFluidHandler(stack, capacityMb));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                return opt.cast();
            }
            return LazyOptional.empty();
        }
    }
}
