package com.minecart.createaddon.block.andesite;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.AndesiteNoteblockEncasedBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AndesiteNoteblockEncasedShaftBlock extends AndesiteNoteblockEncasedBlock implements IBE<AndesiteNoteblockEncasedBlockEntity> {
    public AndesiteNoteblockEncasedShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<AndesiteNoteblockEncasedBlockEntity> getBlockEntityClass() {
        return AndesiteNoteblockEncasedBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AndesiteNoteblockEncasedBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANDESITE_NOTEBLOCK_ENCASED_SHAFT.get();
    }
}
