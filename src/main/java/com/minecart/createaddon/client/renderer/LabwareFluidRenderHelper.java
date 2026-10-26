package com.minecart.createaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;

public final class LabwareFluidRenderHelper {
    /** Narrows the fluid column by 1 px on each horizontal side (2 px shorter side length). */
    private static final float H_INSET = 0.5f / 16f;

    public enum PoseMode {
        /** Block entity / world: fluid AABB is block-local from min corner. */
        BLOCK_ENTITY,
        /** Item custom model: parent already translated to item center (0.5, 0.5, 0.5). */
        ITEM_MODEL_CENTER
    }

    private LabwareFluidRenderHelper() {
    }

    public static void renderFluidColumn(FluidStack fluid, boolean cylinder, float fillFraction, PoseStack poseStack, MultiBufferSource buffer, int light, PoseMode mode) {
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
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, xMin, yBottom, zMin, xMax, yFillTop, zMax, buffer, poseStack, light, false, true);
        if (mode == PoseMode.ITEM_MODEL_CENTER) {
            poseStack.popPose();
        }
    }
}
