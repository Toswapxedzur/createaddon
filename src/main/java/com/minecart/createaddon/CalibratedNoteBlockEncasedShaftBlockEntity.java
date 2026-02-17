package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class CalibratedNoteBlockEncasedShaftBlockEntity extends KineticBlockEntity{
    private int tickTimer = 0;

    public CalibratedNoteBlockEncasedShaftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = 32;
        return 32;
    }

    @Override
    public void tick() {
        super.tick();

        if (level != null && !level.isClientSide) {
            float speed = Math.abs(getSpeed());
            if (speed > 0) {
                tickTimer--;
                if (tickTimer <= 0) {
                    int note = Mth.clamp((int) (speed / 10), 0, 24);

                    level.blockEvent(worldPosition, getBlockState().getBlock(), 67, note);

                    tickTimer = 5;
                }
            }
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("tickTimer", tickTimer);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        tickTimer = compound.getInt("tickTimer");
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        BlockPos.betweenClosedStream(BlockPos.ZERO.below(4), BlockPos.ZERO.above(4)).forEach(
                pos -> {
                    if(pos.getY() != 0)
                        neighbours.add(getBlockPos().offset(pos));
                }
        );
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (other instanceof CalibratedNoteBlockEncasedShaftBlockEntity) {
            BlockPos diff = other.getBlockPos().subtract(this.getBlockPos());
            return diff.getX() == 0 && diff.getZ() == 0 && Math.abs(diff.getY()) <= 4 && this.getBlockState().getValue(BlockStateProperties.AXIS) == other.getBlockState().getValue(BlockStateProperties.AXIS);
        }
        return false;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
                                     boolean connectedViaAxes, boolean connectedViaCogs) {

        if (connectedViaAxes || connectedViaCogs) {
            return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
        }

        if (target instanceof CalibratedNoteBlockEncasedShaftBlockEntity) {
            if (diff.getX() == 0 && diff.getZ() == 0 && Math.abs(diff.getY()) <= 4 && this.getBlockState().getValue(BlockStateProperties.AXIS) == target.getBlockState().getValue(BlockStateProperties.AXIS)) {
                return 1.0f;
            }
        }

        return 0;
    }

    //tooltip

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
                .style(ChatFormatting.GRAY) // Gray text looks best for secondary info
                .forGoggles(tooltip);

        return true;
    }
}
