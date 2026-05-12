package com.minecart.createaddon.block_entities;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class ACNESBlockEntity extends AndesiteNoteblockEncasedBlockEntity {
    public ACNESBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        BlockPos.betweenClosedStream(BlockPos.ZERO.below(4), BlockPos.ZERO.above(4)).forEach(
                pos -> {
                    if (pos.getY() != 0)
                        neighbours.add(getBlockPos().offset(pos));
                }
        );
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (other instanceof ACNESBlockEntity) {
            BlockPos diff = other.getBlockPos().subtract(this.getBlockPos());
            return diff.getX() == 0 && diff.getZ() == 0 && Math.abs(diff.getY()) <= 4
                    && this.getBlockState().getValue(BlockStateProperties.AXIS) == other.getBlockState().getValue(BlockStateProperties.AXIS);
        }
        return false;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
                                     boolean connectedViaAxes, boolean connectedViaCogs) {

        if (connectedViaAxes || connectedViaCogs) {
            return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
        }

        if (target instanceof ACNESBlockEntity) {
            if (diff.getX() == 0 && diff.getZ() == 0 && Math.abs(diff.getY()) <= 4
                    && this.getBlockState().getValue(BlockStateProperties.AXIS) == target.getBlockState().getValue(BlockStateProperties.AXIS)) {
                return 1.0f;
            }
        }

        return 0;
    }
}
