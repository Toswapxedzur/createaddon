package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
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
