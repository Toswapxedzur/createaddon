package com.minecart.createaddon.block_entities.sieve;

import com.minecart.createaddon.ModPartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

/**
 * Vanilla fallback renderer when Flywheel visualization is unavailable.
 * Always renders the contained items on top of the (vibrating) net plane;
 * only renders the moving shaft + net partials when Flywheel is off.
 */
public class MechanicalSieveRenderer extends KineticBlockEntityRenderer<MechanicalSieveBlockEntity> {

    /** Vibration amplitude of the net, measured in blocks. */
    private static final float NET_AMPLITUDE = 0.5f / 16f;
    /** Mesh y-plane is at 8/16; lift items slightly to sit on top. */
    private static final float ITEM_Y = 8.5f / 16f;
    /** Radius of the ring the output items are arranged on, in blocks. */
    private static final float OUTPUT_RING_RADIUS = 3.5f / 16f;
    /** Slight forward tilt (degrees) so the rendered item plane isn't co-planar with the net (avoids z-fighting). */
    private static final float ITEM_TILT_DEG = 7f;
    /** Random horizontal jitter range per stacked layer, in blocks. */
    private static final float STACK_JITTER_RANGE = 1f / 16f;
    /** Per how many items in the stack we add an extra rendered "layer" (mirrors Basin's threshold). */
    private static final int STACK_LAYER_THRESHOLD = 8;

    /**
     * Precomputed local (x, z) positions for displayed items.
     * Index 0 = input (block centre); indices 1..9 = the nine output slots
     * arranged evenly on a ring around the centre.
     * The renderer indexes this directly with output-slot index + 1, so the
     * mapping between slot and on-block position is stable across frames.
     */
    private static final Vector2f[] SLOT_POSITIONS = computeSlotPositions();

    private static Vector2f[] computeSlotPositions() {
        Vector2f[] positions = new Vector2f[10];
        positions[0] = new Vector2f(0.5f, 0.5f);
        for (int i = 0; i < 9; i++) {
            float angle = (float) (2 * Math.PI * i / 9);
            positions[i + 1] = new Vector2f(
                    0.5f + OUTPUT_RING_RADIUS * (float) Math.cos(angle),
                    0.5f + OUTPUT_RING_RADIUS * (float) Math.sin(angle));
        }
        return positions;
    }

    public MechanicalSieveRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MechanicalSieveBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        Direction.Axis axis = facing.getAxis();

        float angle = getAngleForBe(be, be.getBlockPos(), axis);
        float offset = NET_AMPLITUDE * (float) Math.sin(angle);
        // Vibrate perpendicular to the shaft axis, in the horizontal plane.
        float dx = axis == Direction.Axis.Z ? offset : 0f;
        float dz = axis == Direction.Axis.X ? offset : 0f;

        // Items always rendered (Flywheel doesn't draw them); they shake with the net.
        renderItems(be, ms, buffer, light, dx, dz);

        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        SuperByteBuffer netBuffer = CachedBuffers.partialFacing(ModPartialModel.MECHANICAL_SIEVE_NET, state, facing);
        netBuffer.translate(dx, 0, dz)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
    }

    private void renderItems(MechanicalSieveBlockEntity be, PoseStack ms, MultiBufferSource buffer,
                             int light, float dx, float dz) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        // Re-seed per render so jitter is stable per block (matches Basin's pattern).
        RandomSource r = RandomSource.create(be.getBlockPos().hashCode());

        ItemStack input = be.inputInv.getStackInSlot(0);

        for (int i = 0; i < be.outputInv.getSlots(); i++) {
            ItemStack stack = be.outputInv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            // Output slot i takes ring position i + 1 (index 0 is reserved for input).
            renderOne(stack, SLOT_POSITIONS[i + 1], dx, dz, ms, buffer, light, itemRenderer, be, i + 1, r);
        }

        if (!input.isEmpty())
            renderOne(input, SLOT_POSITIONS[0], dx, dz, ms, buffer, light, itemRenderer, be, 0, r);
    }

    private void renderOne(ItemStack stack, Vector2f pos, float dx, float dz, PoseStack ms,
                           MultiBufferSource buffer, int light, ItemRenderer itemRenderer,
                           MechanicalSieveBlockEntity be, int seed, RandomSource r) {
        ms.pushPose();
        ms.translate(pos.x + dx, ITEM_Y, pos.y + dz);

        // Bigger stacks render multiple overlapping copies for a "thicker" pile, à la BasinRenderer.
        int layers = 1 + stack.getCount() / STACK_LAYER_THRESHOLD;
        for (int layer = 0; layer < layers; layer++) {
            ms.pushPose();

            // Apply jitter in WORLD space (before scale/rotation) so y is true vertical;
            // clamp y to be non-negative so the lowest layer never sinks below the net plane.
            Vec3 jitter = VecHelper.offsetRandomly(Vec3.ZERO, r, STACK_JITTER_RANGE);
            ms.translate(jitter.x, Math.abs(jitter.y), jitter.z);

            ms.scale(0.5f, 0.5f, 0.5f);
            // Lay item flat with a slight forward tilt to break co-planarity with the net.
            ms.mulPose(Axis.XP.rotationDegrees(90f - ITEM_TILT_DEG));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    ms, buffer, be.getLevel(), seed * 31 + layer);
            ms.popPose();
        }
        ms.popPose();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MechanicalSieveBlockEntity be, BlockState state) {
        Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        return CachedBuffers.partialFacing(ModPartialModel.MECHANICAL_SIEVE_SHAFT, state, facing);
    }
}
