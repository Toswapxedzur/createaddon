package com.minecart.createaddon.block;

import com.minecart.createaddon.ModBlockEntities;
import com.minecart.createaddon.block_entities.KineticSculkSensorBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
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

    // sculk sensor vanilla

    public static final EnumProperty<SculkSensorPhase> PHASE;
    public static final IntegerProperty POWER;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape SHAPE;
    private static final float[] RESONANCE_PITCH_BEND;

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockpos = ctx.getClickedPos();
        FluidState fluidstate = ctx.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && canActivate(state) && entity.getType() != EntityType.WARDEN) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof KineticSculkSensorBlockEntity sensor && level instanceof ServerLevel serverLevel) {
                if (sensor.getVibrationUser().canReceiveVibration(serverLevel, pos, GameEvent.STEP, GameEvent.Context.of(state))) {
                    sensor.getListener().forceScheduleVibration(serverLevel, GameEvent.STEP, GameEvent.Context.of(entity), entity.position());
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && !state.is(oldState.getBlock()) && state.getValue(POWER) > 0
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.setBlock(pos, state.setValue(POWER, 0), 18);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (getPhase(state) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(level, pos, state);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private static void updateNeighbours(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        level.updateNeighborsAt(pos, block);
        level.updateNeighborsAt(pos.below(), block);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? state.getSignal(level, pos, direction) : 0;
    }

    public static SculkSensorPhase getPhase(BlockState state) {
        return state.getValue(PHASE);
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
        if (!state.getValue(WATERLOGGED)) {
            level.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5,
                    SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F,
                    level.random.nextFloat() * 0.2F + 0.8F);
        }
    }

    public static void tryResonateVibration(@Nullable Entity entity, Level level, BlockPos pos, int frequency) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(BlockTags.VIBRATION_RESONATORS)) {
                level.gameEvent(VibrationSystem.getResonanceEventByFrequency(frequency), blockpos,
                        GameEvent.Context.of(entity, blockstate));
                float f = RESONANCE_PITCH_BEND[frequency];
                level.playSound(null, blockpos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (getPhase(state) == SculkSensorPhase.ACTIVE) {
            Direction direction = Direction.getRandom(random);
            if (direction != Direction.UP && direction != Direction.DOWN) {
                double d0 = (double) pos.getX() + 0.5 + (direction.getStepX() == 0 ? 0.5 - random.nextDouble() : direction.getStepX() * 0.6);
                double d1 = (double) pos.getY() + 0.25;
                double d2 = (double) pos.getZ() + 0.5 + (direction.getStepZ() == 0 ? 0.5 - random.nextDouble() : direction.getStepZ() * 0.6);
                double d3 = random.nextFloat() * 0.04;
                level.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0, d3, 0.0);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{PHASE, POWER, WATERLOGGED});
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KineticSculkSensorBlockEntity sensor) {
            return getPhase(state) == SculkSensorPhase.ACTIVE ? sensor.getLastVibrationFrequency() : 0;
        }
        return 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
    }

    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos,
                          @org.jetbrains.annotations.Nullable BlockEntity blockEntity,
                          @org.jetbrains.annotations.Nullable Entity breaker, ItemStack tool) {
        return 5;
    }

    static {
        PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
        POWER = BlockStateProperties.POWER;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
        RESONANCE_PITCH_BEND = Util.make(new float[16], (arr) -> {
            int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};
            for (int i = 0; i < 16; ++i) {
                arr[i] = NoteBlock.getPitchFromNote(aint[i]);
            }
        });
    }
}
