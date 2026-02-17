package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
