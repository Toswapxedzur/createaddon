package com.minecart.createaddon.client.renderer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CogRenderer extends KineticBlockEntityRenderer<KineticBlockEntity> {
    public CogRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL, state,
                Direction.fromAxisAndDirection(state.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE));
    }
}
