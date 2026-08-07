package com.minecart.createaddon.block.labware;

import com.minecart.createaddon.block_entities.labware.LabwareBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class LabwareBlock<T extends LabwareBlockEntity> extends Block implements IBE<T> {
    private final VoxelShape shape;

    protected LabwareBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) && super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /**
     * Middle-click pick-block path: stamp the BE's fluid + scroll target into the cloned stack.
     * Normal breaking goes through {@link #getDrops}, not this method.
     */
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof LabwareBlockEntity be) {
            be.writeContentsToItem(stack);
        }
        return stack;
    }

    /**
     * Block-break path: 1.20.1's {@link Block#getDrops(BlockState, LootParams.Builder)} runs after
     * loot generation and still has the BE accessible through
     * {@link LootContextParams#BLOCK_ENTITY}, so we copy fluid + scroll target into every
     * self-item drop. Both player breaks and explosions go through this path.
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LabwareBlockEntity labware) {
            Item self = this.asItem();
            for (ItemStack drop : drops) {
                if (drop.getItem() == self) {
                    labware.writeContentsToItem(drop);
                }
            }
        }
        return drops;
    }

    /**
     * Block-place path: the {@link net.minecraft.world.item.BlockItem} placement only restores
     * NBT from {@code BlockEntityTag} on the stack. Our labware data lives under a separate key
     * ({@code LabwareContents}), so we manually feed it back into the freshly-created BE.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof LabwareBlockEntity be) {
            be.readContentsFromItem(stack);
        }
    }
}
