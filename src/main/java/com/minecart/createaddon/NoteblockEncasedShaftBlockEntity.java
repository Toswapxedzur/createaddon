package com.minecart.createaddon;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NoteblockEncasedShaftBlockEntity extends KineticBlockEntity {
    private int tickTimer = 0;

    public NoteblockEncasedShaftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = 16;
        return 16;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
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
}
