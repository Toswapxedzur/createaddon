package com.minecart.createaddon.block_entities.labware;

import com.minecart.createaddon.block_entities.behaviour.LabwareScrollValueBehaviour;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MeasuringCylinderBlockEntity extends LabwareBlockEntity {
    public MeasuringCylinderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 50, 5, 5);
    }

    @Override
    protected Component scrollLabel() {
        return Component.translatable("createaddon.behaviour.measuring_cylinder.fill_level");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        fillLevel = new LabwareScrollValueBehaviour(scrollLabel(), this, new MeasuringCylinderValueBoxTransform());
        behaviours.add(fillLevel);
    }

    public static class MeasuringCylinderValueBoxTransform extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return VecHelper.voxelSpace(8f, 14.05f, 8f);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            TransformStack.of(ms).rotateXDegrees(90);
        }
    }
}
