package com.minecart.createaddon.mixin;

import com.minecart.createaddon.fluid.LabwareItemFluidHandler;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.20.1 Forge port: uses {@link ForgeCapabilities#FLUID_HANDLER_ITEM} via LazyOptional.orElse(null)
 * instead of NeoForge's direct {@code stack.getCapability(...)} returning T or null.
 */
@Mixin(value = GenericItemEmptying.class, remap = false)
public class GenericItemEmptyingMixin {
    @Inject(method = "canItemBeEmptied", at = @At("HEAD"), cancellable = true)
    private static void labwareNoEmpty(Level world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler instanceof LabwareItemFluidHandler labware && !labware.canBeEmptied()) {
            cir.setReturnValue(false);
        }
    }
}
