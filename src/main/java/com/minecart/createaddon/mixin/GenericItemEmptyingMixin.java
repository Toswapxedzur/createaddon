package com.minecart.createaddon.mixin;

import com.minecart.createaddon.fluid.LabwareItemFluidHandler;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenericItemEmptying.class, remap = false)
public class GenericItemEmptyingMixin {
    @Inject(method = "canItemBeEmptied", at = @At("HEAD"), cancellable = true)
    private static void labwareNoEmpty(Level world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler instanceof LabwareItemFluidHandler labware && !labware.canBeEmptied()) {
            cir.setReturnValue(false);
        }
    }
}
