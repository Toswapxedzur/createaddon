package com.minecart.createaddon.block.brass;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.BCNESBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BCNESBlock extends BrassNoteblockEncasedBlock implements IBE<BCNESBlockEntity> {
    public BCNESBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<BCNESBlockEntity> getBlockEntityClass() {
        return BCNESBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BCNESBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BRASS_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT.get();
    }
}
