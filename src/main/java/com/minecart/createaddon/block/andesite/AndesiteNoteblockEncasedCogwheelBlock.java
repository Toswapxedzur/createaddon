package com.minecart.createaddon.block.andesite;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.AndesiteNoteblockEncasedBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AndesiteNoteblockEncasedCogwheelBlock extends AndesiteNoteblockEncasedBlock implements IBE<AndesiteNoteblockEncasedBlockEntity>, ICogWheel {

    public AndesiteNoteblockEncasedCogwheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isLargeCog() {
        return false;
    }

    @Override
    public boolean isSmallCog() {
        return true;
    }

    @Override
    public Class<AndesiteNoteblockEncasedBlockEntity> getBlockEntityClass() {
        return AndesiteNoteblockEncasedBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AndesiteNoteblockEncasedBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANDESITE_NOTEBLOCK_ENCASED_COGWHEEL.get();
    }
}
