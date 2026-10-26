package com.minecart.createaddon.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Item / pick-block data: fluid type (and components) with explicit actual vs target millibuckets.
 * {@link #fluid()} is always empty when {@link #actualMb()} is 0; otherwise its amount equals {@link #actualMb()}.
 */
public record LabwareFluidContents(FluidStack fluid, int actualMb, int targetMb) {
    public static final LabwareFluidContents EMPTY = new LabwareFluidContents(FluidStack.EMPTY, 0, 0);

    public LabwareFluidContents {
        if (fluid.isEmpty() || actualMb <= 0) {
            fluid = FluidStack.EMPTY;
            actualMb = 0;
        } else {
            fluid = fluid.copyWithAmount(actualMb);
        }
    }

    private static LabwareFluidContents fromCodecFields(FluidStack fluid, int actualMbRaw, int targetMb) {
        int actualMb = actualMbRaw >= 0 ? actualMbRaw : (fluid.isEmpty() ? 0 : fluid.getAmount());
        FluidStack f = (fluid.isEmpty() || actualMb <= 0) ? FluidStack.EMPTY : fluid.copyWithAmount(actualMb);
        return new LabwareFluidContents(f, actualMb, targetMb);
    }

    /**
     * Legacy: older data had only {@code fluid} (amount = actual) and {@code target_mb}.
     * When {@code actual_mb} is absent, the fluid stack's amount is used.
     */
    public static final Codec<LabwareFluidContents> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(LabwareFluidContents::fluid),
            Codec.INT.optionalFieldOf("actual_mb", -1).forGetter(LabwareFluidContents::actualMb),
            Codec.INT.optionalFieldOf("target_mb", 0).forGetter(LabwareFluidContents::targetMb)
    ).apply(i, LabwareFluidContents::fromCodecFields));

    public static final StreamCodec<RegistryFriendlyByteBuf, LabwareFluidContents> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.OPTIONAL_STREAM_CODEC, LabwareFluidContents::fluid,
                    ByteBufCodecs.VAR_INT, LabwareFluidContents::actualMb,
                    ByteBufCodecs.VAR_INT, LabwareFluidContents::targetMb,
                    LabwareFluidContents::new);

    public FluidStack fluidForHandler() {
        return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(actualMb);
    }
}
