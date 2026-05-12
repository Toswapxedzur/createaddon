package com.minecart.createaddon.block_entities;

import com.minecart.createaddon.block.KineticSculkSensorBlock;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class KineticSculkSensorBlockEntity extends GeneratingKineticBlockEntity
        implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {
    public int shriekEnergy = 0;

    public KineticSculkSensorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.vibrationData = new VibrationSystem.Data();
        this.vibrationListener = new VibrationSystem.Listener(this);
    }

    @Override
    public float getGeneratedSpeed() {
        Block block = getBlockState().getBlock();
        if (!(block instanceof KineticSculkSensorBlock))
            return 0;
        return shriekEnergy > 15 ? 16 : 0;
    }

    public void shriek() {
        boolean update = false;
        if (getGeneratedSpeed() == 0)
            update = true;
        KineticBlockEntity.switchToBlockState(level, worldPosition,
                getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.ACTIVE));
        shriekEnergy = 40;
        if (update && !level.isClientSide)
            updateGeneratedRotation();
    }

    // sculk sensor block entity

    private static final Logger LOGGER = LogUtils.getLogger();
    private VibrationSystem.Data vibrationData;
    private final VibrationSystem.Listener vibrationListener;
    private final VibrationSystem.User vibrationUser = this.createVibrationUser();
    private int lastVibrationFrequency;

    public VibrationSystem.User createVibrationUser() {
        return new VibrationUser(this.getBlockPos());
    }

    @Override
    public void tick() {
        super.tick();

        if (level instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, vibrationData, vibrationUser);

            if (shriekEnergy > 0) {
                shriekEnergy--;

                if (shriekEnergy == 0 && !level.isClientSide) {
                    KineticBlockEntity.switchToBlockState(level, worldPosition,
                            getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE));
                } else if (shriekEnergy == 10) {
                    KineticBlockEntity.switchToBlockState(level, worldPosition,
                            getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.COOLDOWN));

                    if (!getBlockState().getValue(BlockStateProperties.WATERLOGGED)) {
                        serverLevel.playSound(null, getBlockPos(), SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS,
                                1.0F, level.random.nextFloat() * 0.2F + 0.8F);
                    }
                } else if (shriekEnergy == 15) {
                    updateGeneratedRotation();
                }
            }
        }
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        this.shriekEnergy = compound.getInt("duration");
        this.lastVibrationFrequency = compound.getInt("last_vibration_frequency");
        if (compound.contains("listener", 10) && level != null) {
            RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
            VibrationSystem.Data.CODEC
                    .parse(registryops, compound.getCompound("listener"))
                    .resultOrPartial(s -> LOGGER.error("Failed to parse vibration listener for Sculk Sensor: '{}'", s))
                    .ifPresent(parsed -> this.vibrationData = parsed);
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("duration", this.shriekEnergy);
        compound.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        if (level != null) {
            RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
            VibrationSystem.Data.CODEC
                    .encodeStart(registryops, this.vibrationData)
                    .resultOrPartial(s -> LOGGER.error("Failed to encode vibration listener for Sculk Sensor: '{}'", s))
                    .ifPresent(tag -> compound.put("listener", tag));
        }
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    public void setLastVibrationFrequency(int lastVibrationFrequency) {
        this.lastVibrationFrequency = lastVibrationFrequency;
    }

    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    protected class VibrationUser implements VibrationSystem.User {
        public static final int LISTENER_RANGE = 8;
        protected final BlockPos blockPos;
        private final PositionSource positionSource;

        public VibrationUser(BlockPos pos) {
            this.blockPos = pos;
            this.positionSource = new BlockPositionSource(pos);
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event,
                                           @Nullable GameEvent.Context ctx) {
            return !pos.equals(this.blockPos)
                    || (event != GameEvent.BLOCK_DESTROY && event != GameEvent.BLOCK_PLACE)
                    ? KineticSculkSensorBlock.canActivate(KineticSculkSensorBlockEntity.this.getBlockState())
                    : false;
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event,
                                       @Nullable Entity sourceEntity, @Nullable Entity projectileOwner, float distance) {
            BlockState blockstate = KineticSculkSensorBlockEntity.this.getBlockState();
            if (KineticSculkSensorBlock.canActivate(blockstate)) {
                KineticSculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(event));
                int i = VibrationSystem.getRedstoneStrengthForDistance(distance, this.getListenerRadius());
                if (blockstate.getBlock() instanceof KineticSculkSensorBlock sculksensorblock) {
                    sculksensorblock.activate(sourceEntity, level, this.blockPos, blockstate, i,
                            KineticSculkSensorBlockEntity.this.getLastVibrationFrequency());
                }
            }
        }

        @Override
        public void onDataChanged() {
            KineticSculkSensorBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}
