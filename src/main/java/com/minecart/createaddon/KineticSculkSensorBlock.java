package com.minecart.createaddon;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class KineticSculkSensorBlock extends KineticBlock implements IBE<KineticSculkSensorBlockEntity>, SimpleWaterloggedBlock {
    public KineticSculkSensorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.equals(Direction.DOWN);
    }

    @Override
    public Class<KineticSculkSensorBlockEntity> getBlockEntityClass() {
        return KineticSculkSensorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticSculkSensorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.KINETIC_SCULK_SENSOR.get();
    }

    //sculk sensor vanilla

    public static final MapCodec<KineticSculkSensorBlock> CODEC = simpleCodec(KineticSculkSensorBlock::new);
    public static final EnumProperty<SculkSensorPhase> PHASE;
    public static final IntegerProperty POWER;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape SHAPE;
    private static final float[] RESONANCE_PITCH_BEND;

    public MapCodec<? extends KineticSculkSensorBlock> codec() {
        return CODEC;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_154396_) {
        BlockPos blockpos = p_154396_.getClickedPos();
        FluidState fluidstate = p_154396_.getLevel().getFluidState(blockpos);
        return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    protected FluidState getFluidState(BlockState p_154479_) {
        return (Boolean)p_154479_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_154479_);
    }

    public void stepOn(Level p_222132_, BlockPos p_222133_, BlockState p_222134_, Entity p_222135_) {
        if (!p_222132_.isClientSide() && canActivate(p_222134_) && p_222135_.getType() != EntityType.WARDEN) {
            BlockEntity var7 = p_222132_.getBlockEntity(p_222133_);
            if (var7 instanceof KineticSculkSensorBlockEntity) {
                KineticSculkSensorBlockEntity sculksensorblockentity = (KineticSculkSensorBlockEntity)var7;
                if (p_222132_ instanceof ServerLevel) {
                    ServerLevel serverlevel = (ServerLevel)p_222132_;
                    if (sculksensorblockentity.getVibrationUser().canReceiveVibration(serverlevel, p_222133_, GameEvent.STEP, GameEvent.Context.of(p_222134_))) {
                        sculksensorblockentity.getListener().forceScheduleVibration(serverlevel, GameEvent.STEP, GameEvent.Context.of(p_222135_), p_222135_.position());
                    }
                }
            }
        }

        super.stepOn(p_222132_, p_222133_, p_222134_, p_222135_);
    }

    public void onPlace(BlockState p_154471_, Level p_154472_, BlockPos p_154473_, BlockState p_154474_, boolean p_154475_) {
        if (!p_154472_.isClientSide() && !p_154471_.is(p_154474_.getBlock()) && (Integer)p_154471_.getValue(POWER) > 0 && !p_154472_.getBlockTicks().hasScheduledTick(p_154473_, this)) {
            p_154472_.setBlock(p_154473_, (BlockState)p_154471_.setValue(POWER, 0), 18);
        }

    }

    public void onRemove(BlockState p_154446_, Level p_154447_, BlockPos p_154448_, BlockState p_154449_, boolean p_154450_) {
        if (!p_154446_.is(p_154449_.getBlock())) {
            super.onRemove(p_154446_, p_154447_, p_154448_, p_154449_, p_154450_);
            if (getPhase(p_154446_) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(p_154447_, p_154448_, p_154446_);
            }
        }

    }

    protected BlockState updateShape(BlockState p_154457_, Direction p_154458_, BlockState p_154459_, LevelAccessor p_154460_, BlockPos p_154461_, BlockPos p_154462_) {
        if ((Boolean)p_154457_.getValue(WATERLOGGED)) {
            p_154460_.scheduleTick(p_154461_, Fluids.WATER, Fluids.WATER.getTickDelay(p_154460_));
        }

        return super.updateShape(p_154457_, p_154458_, p_154459_, p_154460_, p_154461_, p_154462_);
    }

    private static void updateNeighbours(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        level.updateNeighborsAt(pos, block);
        level.updateNeighborsAt(pos.below(), block);
    }

    protected RenderShape getRenderShape(BlockState p_154477_) {
        return RenderShape.MODEL;
    }

    protected VoxelShape getShape(BlockState p_154432_, BlockGetter p_154433_, BlockPos p_154434_, CollisionContext p_154435_) {
        return SHAPE;
    }

    protected boolean isSignalSource(BlockState p_154484_) {
        return true;
    }

    protected int getSignal(BlockState p_154437_, BlockGetter p_154438_, BlockPos p_154439_, Direction p_154440_) {
        return (Integer)p_154437_.getValue(POWER);
    }

    public int getDirectSignal(BlockState p_279407_, BlockGetter p_279217_, BlockPos p_279190_, Direction p_279273_) {
        return p_279273_ == Direction.UP ? p_279407_.getSignal(p_279217_, p_279190_, p_279273_) : 0;
    }

    public static SculkSensorPhase getPhase(BlockState state) {
        return (SculkSensorPhase)state.getValue(PHASE);
    }

    public static boolean canActivate(BlockState state) {
        return true;
    }

    public int getActiveTicks() {
        return 30;
    }

    public void activate(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, int power, int frequency) {
        withBlockEntityDo(level, pos, be -> be.shriek());
        updateNeighbours(level, pos, state);
        tryResonateVibration(entity, level, pos, frequency);
        level.gameEvent(entity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, pos);
        if (!(Boolean)state.getValue(WATERLOGGED)) {
            level.playSound((Player)null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);
        }
    }

    public static void tryResonateVibration(@Nullable Entity entity, Level level, BlockPos pos, int frequency) {
        for(Direction direction : Direction.values()) {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(BlockTags.VIBRATION_RESONATORS)) {
                level.gameEvent(VibrationSystem.getResonanceEventByFrequency(frequency), blockpos, GameEvent.Context.of(entity, blockstate));
                float f = RESONANCE_PITCH_BEND[frequency];
                level.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
            }
        }

    }

    public void animateTick(BlockState p_222148_, Level p_222149_, BlockPos p_222150_, RandomSource p_222151_) {
        if (getPhase(p_222148_) == SculkSensorPhase.ACTIVE) {
            Direction direction = Direction.getRandom(p_222151_);
            if (direction != Direction.UP && direction != Direction.DOWN) {
                double d0 = (double)p_222150_.getX() + (double)0.5F + (direction.getStepX() == 0 ? (double)0.5F - p_222151_.nextDouble() : (double)direction.getStepX() * 0.6);
                double d1 = (double)p_222150_.getY() + (double)0.25F;
                double d2 = (double)p_222150_.getZ() + (double)0.5F + (direction.getStepZ() == 0 ? (double)0.5F - p_222151_.nextDouble() : (double)direction.getStepZ() * 0.6);
                double d3 = (double)p_222151_.nextFloat() * 0.04;
                p_222149_.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, (double)0.0F, d3, (double)0.0F);
            }
        }

    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_154464_) {
        p_154464_.add(new Property[]{PHASE, POWER, WATERLOGGED});
    }

    protected boolean hasAnalogOutputSignal(BlockState p_154481_) {
        return false;
    }

    protected int getAnalogOutputSignal(BlockState p_154442_, Level p_154443_, BlockPos p_154444_) {
        BlockEntity var5 = p_154443_.getBlockEntity(p_154444_);
        if (var5 instanceof KineticSculkSensorBlockEntity sculksensorblockentity) {
            return getPhase(p_154442_) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
        } else {
            return 0;
        }
    }

    protected boolean isPathfindable(BlockState p_154427_, PathComputationType p_154430_) {
        return false;
    }

    protected boolean useShapeForLightOcclusion(BlockState p_154486_) {
        return true;
    }

    protected void spawnAfterBreak(BlockState p_222142_, ServerLevel p_222143_, BlockPos p_222144_, ItemStack p_222145_, boolean p_222146_) {
        super.spawnAfterBreak(p_222142_, p_222143_, p_222144_, p_222145_, p_222146_);
    }

    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @org.jetbrains.annotations.Nullable BlockEntity blockEntity, @org.jetbrains.annotations.Nullable Entity breaker, ItemStack tool) {
        return 5;
    }

    static {
        PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
        POWER = BlockStateProperties.POWER;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)8.0F, (double)16.0F);
        RESONANCE_PITCH_BEND = (float[]) Util.make(new float[16], (p_277301_) -> {
            int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

            for(int i = 0; i < 16; ++i) {
                p_277301_[i] = NoteBlock.getPitchFromNote(aint[i]);
            }

        });
    }
}
