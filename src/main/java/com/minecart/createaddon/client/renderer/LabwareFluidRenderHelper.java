package com.minecart.createaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraftforge.fluids.FluidStack;

/**
 * 1.20.1 Forge port: uses {@link ForgeCatnipServices#FLUID_RENDERER} (strongly typed for
 * Forge {@link FluidStack}) instead of NeoForge-specific {@code NeoForgeCatnipServices.FLUID_RENDERER}.
 * The platform-neutral {@code CatnipServices.FLUID_RENDERER} is {@code FluidRenderHelper<?>} which
 * can't be invoked with {@code FluidStack} directly.
 */
public final class LabwareFluidRenderHelper {
    private static final float H_INSET = 0.5f / 16f;

    public enum PoseMode {
        BLOCK_ENTITY,
        ITEM_MODEL_CENTER
    }

    private LabwareFluidRenderHelper() {
    }

    public static void renderFluidColumn(FluidStack fluid, boolean cylinder, float fillFraction, PoseStack poseStack,
                                         MultiBufferSource buffer, int light, PoseMode mode) {
        if (fluid.isEmpty()) {
            return;
        }
        float frac = Mth.clamp(fillFraction, 0, 1);
        if (frac <= 0) {
            return;
        }

        float xMin = (cylinder ? 6f : 5f) / 16f + H_INSET;
        float xMax = (cylinder ? 10f : 11f) / 16f - H_INSET;
        float zMin = xMin;
        float zMax = xMax;
        float yBottom = cylinder ? 1.5f / 16f : 0.5f / 16f;
        float yTop = cylinder ? 13.5f / 16f : 7.5f / 16f;
        float yFillTop = yBottom + (yTop - yBottom) * frac;

        if (mode == PoseMode.ITEM_MODEL_CENTER) {
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.5f, -0.5f);
        }
        ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, xMin, yBottom, zMin, xMax, yFillTop, zMax, buffer, poseStack, light, false, true);
        if (mode == PoseMode.ITEM_MODEL_CENTER) {
            poseStack.popPose();
        }
    }
}
