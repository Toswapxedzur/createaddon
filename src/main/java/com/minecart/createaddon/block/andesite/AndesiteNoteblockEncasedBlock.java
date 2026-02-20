package com.minecart.createaddon.block.andesite;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AndesiteNoteblockEncasedBlock extends RotatedPillarKineticBlock {

    public AndesiteNoteblockEncasedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 67) {
            float pitch = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);

            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.NOTE_BLOCK_HARP.value(),
                    SoundSource.RECORDS,
                    3.0F,
                    pitch,
                    false
            );

            level.addParticle(
                    ParticleTypes.NOTE,
                    pos.getX() + 0.5D,
                    pos.getY() + 1.2D,
                    pos.getZ() + 0.5D,
                    (double) param / 24.0D,
                    0.0D,
                    0.0D
            );

            return true;
        }

        return super.triggerEvent(state, level, pos, id, param);
    }
}
