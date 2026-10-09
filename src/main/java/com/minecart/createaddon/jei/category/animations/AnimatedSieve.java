package com.minecart.createaddon.jei.category.animations;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.ModPartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.client.gui.GuiGraphics;

/**
 * JEI preview animation for the {@link com.minecart.createaddon.block_entities.sieve.MechanicalSieveBlockEntity}.
 * Renders the static frame, the constantly-spinning shaft (with wooden cams), and the
 * net oscillating perpendicular to the shaft axis. Amplitude is exaggerated relative
 * to the in-game value so the motion is legible at JEI's small render scale.
 */
public class AnimatedSieve extends AnimatedKinetics {

    /** Net oscillation amplitude in block units (exaggerated for JEI legibility). */
    private static final float NET_AMPLITUDE = 1.5f / 16f;

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        // Looking-down iso view so the net + shaft cams are clearly visible.
        matrixStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        float angleDeg = getCurrentAngle();
        float netOffset = NET_AMPLITUDE * (float) Math.sin(Math.toRadians(angleDeg));

        // Spinning shaft (model includes the wooden cams for the linkage look).
        blockElement(ModPartialModel.MECHANICAL_SIEVE_SHAFT)
                .rotateBlock(0, 0, angleDeg)
                .scale(scale)
                .render(graphics);

        // Static frame (the block.json mesh, no shaft or net).
        blockElement(ModBlocks.MECHANICAL_SIEVE.getDefaultState())
                .scale(scale)
                .render(graphics);

        // Oscillating net translates along X (perpendicular to default-facing Z shaft).
        blockElement(ModPartialModel.MECHANICAL_SIEVE_NET)
                .atLocal(netOffset, 0, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}
