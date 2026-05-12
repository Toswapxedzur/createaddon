package com.minecart.createaddon.block.labware;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.labware.MeasuringCylinderBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MeasuringCylinderBlock extends LabwareBlock<MeasuringCylinderBlockEntity> {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 14, 11);

    public MeasuringCylinderBlock(Properties properties) {
        super(properties, SHAPE);
    }

    @Override
    public Class<MeasuringCylinderBlockEntity> getBlockEntityClass() {
        return MeasuringCylinderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MeasuringCylinderBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MEASURING_CYLINDER.get();
    }
}
