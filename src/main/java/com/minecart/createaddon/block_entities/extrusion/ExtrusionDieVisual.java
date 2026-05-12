package com.minecart.createaddon.block_entities.extrusion;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ExtrusionDieVisual extends KineticBlockEntityVisual<ExtrusionDieBlockEntity> {

    public ExtrusionDieVisual(VisualizationContext context, ExtrusionDieBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Override
    protected void _delete() {
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
    }

    @Override
    public void updateLight(float partialTick) {
    }
}
