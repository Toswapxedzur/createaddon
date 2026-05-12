package com.minecart.createaddon.block_entities;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BrassNoteblockEncasedBlockEntity extends KineticBlockEntity {

    protected ScrollValueBehaviour volumeScroll;
    private int tickTimer = 0;

    private static final String[] NOTE_NAMES = {"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"};

    public BrassNoteblockEncasedBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        volumeScroll = new ScrollValueBehaviour(Component.translatable("createaddon.behaviour.volume"), this, new NoteblockValueBoxTransform());
        volumeScroll.between(0, 100);
        volumeScroll.value = 75; // Default Minecraft Note Block volume is 3.0
        volumeScroll.withFormatter(i -> i + "%"); // 0%, 25%, 50%... 100%
        volumeScroll.withCallback(this::onValueChanged);

        behaviours.add(volumeScroll);
        super.addBehaviours(behaviours);
    }

    public void onValueChanged(int integer) {
        detachKinetics();
        updateSpeed = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (level != null && !level.isClientSide) {
            float speed = Math.abs(getSpeed());
            if (speed > 0) {
                tickTimer--;
                if (tickTimer <= 0) {
                    playNote(speed);
                    tickTimer = 5;
                }
            }
        }
    }

    private void playNote(float speed) {
        int noteId = Mth.clamp((int) (speed / 10), 0, 24);
        float pitch = (float) Math.pow(2.0D, (double) (noteId - 12) / 12.0D);

        float volume = (float) volumeScroll.getValue() / 100;

        if (volume > 0) {
            NoteBlockInstrument instrument = NoteBlockInstrument.HARP; // Default to Harp/Piano

            level.playSound(null, worldPosition, instrument.getSoundEvent().value(), SoundSource.RECORDS, volume, pitch);

            if (level instanceof ServerLevel serverLevel) {
                double r = (double) noteId / 24.0D;
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 1.2D,
                        worldPosition.getZ() + 0.5D,
                        0, r, 0.0D, 0.0D, 1.0D);
            }
        }
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToTooltip(tooltip, isPlayerSneaking);

        float speed = Math.abs(getSpeed());

        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.speed",
                        CreateLang.number(speed).component().withStyle(ChatFormatting.WHITE))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float speed = Math.abs(getSpeed());
        int noteIndex = Mth.clamp((int) (speed / 10), 0, 24);
        String noteName = NOTE_NAMES[noteIndex % 12];
        boolean superResult = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        int frequency = (speed > 0) ? 4 : 0;

        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.tune",
                        CreateLang.text(noteName).style(ChatFormatting.LIGHT_PURPLE))
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);

        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.frequency",
                        CreateLang.number(frequency).component().withStyle(ChatFormatting.BLUE))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        int volPercent = volumeScroll.getValue();
        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.volumn")
                .style(ChatFormatting.GREEN)
                .add(CreateLang.text(volPercent + "%").style(ChatFormatting.DARK_GREEN))
                .forGoggles(tooltip);

        return true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("tickTimer", tickTimer);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tickTimer = compound.getInt("tickTimer");
    }

    private class NoteblockValueBoxTransform extends ValueBoxTransform.Sided {
        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction.Axis shaftAxis = state.getValue(BlockStateProperties.AXIS);

            return direction.getAxis() != shaftAxis;
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 16.05f);
        }
    }
}
