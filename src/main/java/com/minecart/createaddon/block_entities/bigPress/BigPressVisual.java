package com.minecart.createaddon.block_entities.bigPress;

import com.minecart.createaddon.ModPartialModel;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class BigPressVisual extends ShaftVisual<BigPressBlockEntity> implements SimpleDynamicVisual {

    private final OrientedInstance pressHead;

    public BigPressVisual(VisualizationContext context, BigPressBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        pressHead = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(ModPartialModel.BIG_PRESS_HEAD))
                .createInstance();

        Quaternionf q = Axis.YP
                .rotationDegrees(AngleHelper.horizontalAngle(blockState.getValue(BigPressBlock.HORIZONTAL_FACING)));

        pressHead.rotation(q);

        transformModels(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        transformModels(ctx.partialTick());
    }

    private void transformModels(float pt) {
        CompressingBehaviour behaviour = blockEntity.getCompressingBehaviour();

        float renderedHeadOffset = behaviour == null ? 0 : behaviour.getRenderedHeadOffset(pt);
        float extensionLength = behaviour == null ? 0 : behaviour.getExtensionLength(pt);

        pressHead.position(getVisualPosition())
                .translatePosition(0, -renderedHeadOffset * extensionLength, 0)
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pressHead);
    }

    @Override
    protected void _delete() {
        super._delete();
        pressHead.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pressHead);
    }
}
