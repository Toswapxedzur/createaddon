package com.minecart.createaddon.client.renderer;

import com.minecart.createaddon.block_entities.labware.LabwareBlockEntity;
import com.minecart.createaddon.block_entities.labware.MeasuringCylinderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraftforge.fluids.FluidStack;

public class LabwareRenderer extends SafeBlockEntityRenderer<LabwareBlockEntity> {

    public LabwareRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(LabwareBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = be.getTankFluidForRender();
        if (fluid.isEmpty()) {
            return;
        }
        int cap = be.getLabwareCapacityMb();
        if (cap <= 0) {
            return;
        }
        float frac = Mth.clamp(fluid.getAmount() / (float) cap, 0, 1);
        if (frac <= 0) {
            return;
        }

        boolean cylinder = be instanceof MeasuringCylinderBlockEntity;
        LabwareFluidRenderHelper.renderFluidColumn(fluid, cylinder, frac, ms, buffer, light, LabwareFluidRenderHelper.PoseMode.BLOCK_ENTITY);
    }
}
