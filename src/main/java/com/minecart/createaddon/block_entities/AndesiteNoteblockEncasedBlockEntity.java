package com.minecart.createaddon.block_entities;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class AndesiteNoteblockEncasedBlockEntity extends KineticBlockEntity {
    public AndesiteNoteblockEncasedBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    private int tickTimer = 0;

    @Override
    public void tick() {
        super.tick();

        if (level != null && !level.isClientSide) {
            float speed = Math.abs(getSpeed());
            if (speed > 0) {
                tickTimer--;
                if (tickTimer <= 0) {
                    // Map speed (0-256) to note range (0-24)
                    // Adjust the divisor (10) to change how fast the pitch rises with RPM
                    int note = Mth.clamp((int) (speed / 10), 0, 24);

                    level.blockEvent(worldPosition, getBlockState().getBlock(), 67, note);

                    // Reset timer. 5 ticks = 4 notes per second.
                    tickTimer = 5;
                }
            }
        }
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

    // tooltip
    private static final String[] NOTE_NAMES = {"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"};

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
        boolean superResult = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        float speed = Math.abs(getSpeed());
        int noteIndex = Mth.clamp((int) (speed / 10), 0, 24);
        String noteName = NOTE_NAMES[noteIndex % 12];

        int frequency = (speed > 0) ? 4 : 0;

        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.tune",
                        CreateLang.text(noteName).style(ChatFormatting.LIGHT_PURPLE))
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);

        CreateLang.translate("createaddon.tooltip.kinetic_noteblock.frequency",
                        CreateLang.number(frequency).component().withStyle(ChatFormatting.BLUE))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        return true;
    }
}
