package com.minecart.createaddon.block.brass;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.BrassNoteblockEncasedBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BrassNoteblockEncasedCogwheelBlock extends BrassNoteblockEncasedBlock implements IBE<BrassNoteblockEncasedBlockEntity>, ICogWheel {
    public BrassNoteblockEncasedCogwheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<BrassNoteblockEncasedBlockEntity> getBlockEntityClass() {
        return BrassNoteblockEncasedBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BrassNoteblockEncasedBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BRASS_NOTEBLOCK_ENCASED_COGWHEEL.get();
    }

    @Override
    public boolean isLargeCog() {
        return false;
    }
}
