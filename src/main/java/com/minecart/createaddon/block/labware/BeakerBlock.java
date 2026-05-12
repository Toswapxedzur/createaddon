package com.minecart.createaddon.block.labware;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.labware.BeakerBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeakerBlock extends LabwareBlock<BeakerBlockEntity> {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public BeakerBlock(Properties properties) {
        super(properties, SHAPE);
    }

    @Override
    public Class<BeakerBlockEntity> getBlockEntityClass() {
        return BeakerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeakerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BEAKER.get();
    }
}
