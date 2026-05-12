package com.minecart.createaddon.block_entities.sieve;

import com.minecart.createaddon.ModPartialModel;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class MechanicalSieveVisual extends SingleAxisRotatingVisual<MechanicalSieveBlockEntity>
        implements SimpleDynamicVisual {

    private static final float NET_AMPLITUDE = 0.5f / 16f;

    private final OrientedInstance net;

    public MechanicalSieveVisual(VisualizationContext context, MechanicalSieveBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, Models.partial(ModPartialModel.MECHANICAL_SIEVE_SHAFT));

        Direction facing = blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        Quaternionf facingRotation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing));

        net = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(ModPartialModel.MECHANICAL_SIEVE_NET))
                .createInstance();
        net.rotation(facingRotation);

        transformModels(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        transformModels(ctx.partialTick());
    }

    private void transformModels(float partialTick) {
        Direction.Axis axis = blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getAxis();
        float angle = KineticBlockEntityRenderer.getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);
        float offset = NET_AMPLITUDE * (float) Math.sin(angle);

        float dx = axis == Direction.Axis.Z ? offset : 0f;
        float dz = axis == Direction.Axis.X ? offset : 0f;

        net.position(getVisualPosition())
                .translatePosition(dx, 0, dz)
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(net);
    }

    @Override
    protected void _delete() {
        super._delete();
        net.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(net);
    }
}
