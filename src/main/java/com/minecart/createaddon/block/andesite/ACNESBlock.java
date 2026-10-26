package com.minecart.createaddon.block.andesite;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.ACNESBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ACNESBlock extends AndesiteNoteblockEncasedBlock implements IBE<ACNESBlockEntity> {
    public ACNESBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ACNESBlockEntity> getBlockEntityClass() {
        return ACNESBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ACNESBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANDESITE_CALIBRATED_NOTEBLOCK_ENCASED_SHAFT.get();
    }
}
