package com.minecart.createaddon.jei.category.animations;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.ModPartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 1.20.1 port: unchanged from the 1.21.1 source — the {@link AnimatedKinetics} entry points
 * we use (block / partial model render builders, {@code getCurrentAngle()}) are the same.
 */
public class AnimatedSieve extends AnimatedKinetics {

    private static final float NET_AMPLITUDE = 1.5f / 16f;

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        float angleDeg = getCurrentAngle();
        float netOffset = NET_AMPLITUDE * (float) Math.sin(Math.toRadians(angleDeg));

        blockElement(ModPartialModel.MECHANICAL_SIEVE_SHAFT)
                .rotateBlock(0, 0, angleDeg)
                .scale(scale)
                .render(graphics);

        blockElement(ModBlocks.MECHANICAL_SIEVE.getDefaultState())
                .scale(scale)
                .render(graphics);

        blockElement(ModPartialModel.MECHANICAL_SIEVE_NET)
                .atLocal(netOffset, 0, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}
