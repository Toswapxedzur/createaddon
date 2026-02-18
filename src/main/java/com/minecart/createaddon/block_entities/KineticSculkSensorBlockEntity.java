package com.minecart.createaddon.block_entities;

import com.minecart.createaddon.block.KineticSculkSensorBlock;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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

public class KineticSculkSensorBlockEntity extends GeneratingKineticBlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
    public int shriekEnergy = 0;

    public KineticSculkSensorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.vibrationData = new VibrationSystem.Data();
        this.vibrationListener = new VibrationSystem.Listener(this);
    }

    @Override
    public float getGeneratedSpeed() {
        Block block = getBlockState().getBlock();
        if (!(block instanceof KineticSculkSensorBlock b))
            return 0;
        return shriekEnergy > 15 ? 16 : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = shriekEnergy > 15 ? 16 : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    public void shriek(){
        boolean update = false;
        if(getGeneratedSpeed() == 0)
            update = true;
        KineticBlockEntity.switchToBlockState(level, worldPosition, getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.ACTIVE));
        shriekEnergy = 40;
        if(update && !level.isClientSide)
            updateGeneratedRotation();
    }

    //sculk sensor block entity

    private static final Logger LOGGER = LogUtils.getLogger();
    private VibrationSystem.Data vibrationData;
    private final VibrationSystem.Listener vibrationListener;
    private final VibrationSystem.User vibrationUser = this.createVibrationUser();
    private int lastVibrationFrequency;

    public VibrationSystem.User createVibrationUser() {
        return new KineticSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
    }

    @Override
    public void tick() {
        super.tick();

        if(level instanceof ServerLevel serverLevel){
            VibrationSystem.Ticker.tick(serverLevel, vibrationData, vibrationUser);

            if(shriekEnergy > 0) {
                shriekEnergy--;

                if(shriekEnergy == 0 && !level.isClientSide){
                    KineticBlockEntity.switchToBlockState(level, worldPosition, getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE));
                }else if(shriekEnergy == 10){
                    KineticBlockEntity.switchToBlockState(level, worldPosition, getBlockState().setValue(BlockStateProperties.SCULK_SENSOR_PHASE, SculkSensorPhase.COOLDOWN));

                    if (!(Boolean)getBlockState().getValue(BlockStateProperties.WATERLOGGED)) {
                        serverLevel.playSound((Player)null, getBlockPos(), SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);
                    }
                }else if(shriekEnergy == 15){
                    updateGeneratedRotation();
                }
            }
        }

    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.shriekEnergy = compound.getInt("duration");
        this.lastVibrationFrequency = compound.getInt("last_vibration_frequency");
        RegistryOps<Tag> registryops = registries.createSerializationContext(NbtOps.INSTANCE);
        if (compound.contains("listener", 10)) {
            VibrationSystem.Data.CODEC
                    .parse(registryops, compound.getCompound("listener"))
                    .resultOrPartial(p_351973_ -> LOGGER.error("Failed to parse vibration listener for Sculk Sensor: '{}'", p_351973_))
                    .ifPresent(p_281146_ -> this.vibrationData = p_281146_);
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("duration", this.shriekEnergy);
        compound.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        RegistryOps<Tag> registryops = registries.createSerializationContext(NbtOps.INSTANCE);
        VibrationSystem.Data.CODEC
                .encodeStart(registryops, this.vibrationData)
                .resultOrPartial(p_351974_ -> LOGGER.error("Failed to encode vibration listener for Sculk Sensor: '{}'", p_351974_))
                .ifPresent(p_222820_ -> compound.put("listener", p_222820_));
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
        public boolean canReceiveVibration(ServerLevel p_282127_, BlockPos p_283268_, Holder<GameEvent> p_316115_, @Nullable GameEvent.Context p_282856_) {
            return !p_283268_.equals(this.blockPos) || !p_316115_.is(GameEvent.BLOCK_DESTROY) && !p_316115_.is(GameEvent.BLOCK_PLACE)
                    ? KineticSculkSensorBlock.canActivate(KineticSculkSensorBlockEntity.this.getBlockState())
                    : false;
        }

        @Override
        public void onReceiveVibration(
                ServerLevel p_282851_, BlockPos p_281608_, Holder<GameEvent> p_316423_, @Nullable Entity p_282123_, @Nullable Entity p_283090_, float p_283130_
        ) {
            BlockState blockstate = KineticSculkSensorBlockEntity.this.getBlockState();
            if (KineticSculkSensorBlock.canActivate(blockstate)) {
                KineticSculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(p_316423_));
                int i = VibrationSystem.getRedstoneStrengthForDistance(p_283130_, this.getListenerRadius());
                if (blockstate.getBlock() instanceof KineticSculkSensorBlock sculksensorblock) {
                    sculksensorblock.activate(p_282123_, p_282851_, this.blockPos, blockstate, i, KineticSculkSensorBlockEntity.this.getLastVibrationFrequency());
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
