package com.minecart.createaddon.block;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.NoteblockEncasedShaftBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class NoteblockEncasedShaftBlock extends NoteblockEncasedBlock implements IBE<NoteblockEncasedShaftBlockEntity> {
    public NoteblockEncasedShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<NoteblockEncasedShaftBlockEntity> getBlockEntityClass() {
        return NoteblockEncasedShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NoteblockEncasedShaftBlockEntity> getBlockEntityType() {
        return ModBlockEntities.noteblock_ENCASED_SHAFT.get();
    }
}
