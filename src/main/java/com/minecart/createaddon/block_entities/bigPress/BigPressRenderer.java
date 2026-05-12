package com.minecart.createaddon.block_entities.bigPress;

import com.minecart.createaddon.ModPartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class BigPressRenderer extends KineticBlockEntityRenderer<BigPressBlockEntity> {
    public BigPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(BigPressBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(BigPressBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        BlockState blockState = be.getBlockState();
        CompressingBehaviour behaviour = be.getCompressingBehaviour();

        float renderedHeadOffset = behaviour == null ? 0 : behaviour.getRenderedHeadOffset(partialTicks);
        float extensionLength = behaviour == null ? 0 : behaviour.getExtensionLength(partialTicks);

        SuperByteBuffer cachedHead = CachedBuffers.partialFacing(ModPartialModel.BIG_PRESS_HEAD, blockState, blockState.getValue(BigPressBlock.HORIZONTAL_FACING));

        cachedHead
                .translate(0, -renderedHeadOffset * extensionLength, 0)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(BigPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
