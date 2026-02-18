package com.minecart.createaddon.block;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.NoteblockEncasedCogwheelBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class NoteblockEncasedCogwheelBlock extends NoteblockEncasedBlock implements IBE<NoteblockEncasedCogwheelBlockEntity>, ICogWheel {

    public NoteblockEncasedCogwheelBlock(Properties properties) {
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
    public Class<NoteblockEncasedCogwheelBlockEntity> getBlockEntityClass() {
        return NoteblockEncasedCogwheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NoteblockEncasedCogwheelBlockEntity> getBlockEntityType() {
        return ModBlockEntities.noteblock_ENCASED_COGWHEEL.get();
    }
}
