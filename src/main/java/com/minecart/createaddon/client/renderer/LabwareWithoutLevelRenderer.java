package com.minecart.createaddon.client.renderer;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.fluid.LabwareFluidContents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class LabwareWithoutLevelRenderer extends CustomRenderedItemModelRenderer {
    public LabwareWithoutLevelRenderer() {
    }

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        boolean cylinder = block == ModBlocks.MEASURING_CYLINDER.get();

        CompoundTag tag = stack.getTag();
        LabwareFluidContents contents = (tag != null && tag.contains("LabwareContents"))
                ? LabwareFluidContents.read(tag.getCompound("LabwareContents"))
                : LabwareFluidContents.EMPTY;
        FluidStack fluid = contents.fluidForHandler();
        int cap = cylinder ? 50 : 250;
        float frac = cap > 0 ? Mth.clamp(contents.actualMb() / (float) cap, 0, 1) : 0;
        LabwareFluidRenderHelper.renderFluidColumn(fluid, cylinder, frac, ms, buffer, light, LabwareFluidRenderHelper.PoseMode.ITEM_MODEL_CENTER);

        renderer.render(model.getOriginalModel(), light);
    }
}
