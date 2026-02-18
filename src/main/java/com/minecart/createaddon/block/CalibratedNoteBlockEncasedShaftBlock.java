package com.minecart.createaddon.block;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.CalibratedNoteBlockEncasedShaftBlockEntity;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CalibratedNoteBlockEncasedShaftBlock extends RotatedPillarKineticBlock implements IBE<CalibratedNoteBlockEncasedShaftBlockEntity>{
    public CalibratedNoteBlockEncasedShaftBlock(BlockBehaviour.Properties properties) {
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
    public Class<CalibratedNoteBlockEncasedShaftBlockEntity> getBlockEntityClass() {
        return CalibratedNoteBlockEncasedShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CalibratedNoteBlockEncasedShaftBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CALIBRATED_NOTEBLOCK_ENCASED_SHAFT.get();
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 67) {
            float pitch = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);

            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
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
