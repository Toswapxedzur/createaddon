package com.minecart.createaddon;

import com.minecart.createaddon.fluid.LabwareFluidContents;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(CreateAddon.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LabwareFluidContents>> LABWARE_CONTENTS =
            REGISTER.registerComponentType(
                    "labware_contents",
                    builder -> builder
                            .persistent(LabwareFluidContents.CODEC)
                            .networkSynchronized(LabwareFluidContents.STREAM_CODEC));

    private ModDataComponents() {
    }
}
