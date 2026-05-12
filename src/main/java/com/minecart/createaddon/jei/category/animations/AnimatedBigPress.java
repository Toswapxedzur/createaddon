package com.minecart.createaddon.jei.category.animations;

import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.ModPartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * 1.20.1 port: identical to the 1.21.1 source. {@link AnimatedKinetics} is the same in
 * Create 6.0.8 (1.20.1) as in 6.0.9 (1.21.1) for the methods we use.
 */
public class AnimatedBigPress extends AnimatedKinetics {
    private static final int BREAKTIME = 80;
    private final int displayDuration;

    public AnimatedBigPress(int duration) {
        this.displayDuration = Math.max(1, duration) / 4;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        blockElement(shaft(Direction.Axis.Z))
                .rotateBlock(0, 0, getCurrentAngle())
                .scale(scale)
                .render(graphics);

        blockElement(ModBlocks.BIGPRESS.getDefaultState())
                .atLocal(0, 0, 0)
                .scale(scale)
                .render(graphics);

        blockElement(ModPartialModel.BIG_PRESS_HEAD)
                .atLocal(0, -getAnimatedHeadOffset(), 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }

    private float getAnimatedHeadOffset() {
        float time = AnimationTickHolder.getRenderTime() - offset * 8;
        float cycle = time % (displayDuration + BREAKTIME * 2);

        float verticalOffset;

        if (cycle < BREAKTIME) {
            float progress = cycle / BREAKTIME;
            verticalOffset = -Mth.clamp(progress * progress * progress, 0, 1);
        } else if (cycle < BREAKTIME + displayDuration) {
            verticalOffset = -1;
        } else {
            float progress = (cycle - displayDuration - BREAKTIME) / BREAKTIME;
            verticalOffset = Mth.clamp(progress, 0, 1) - 1;
        }

        return verticalOffset * 13f / 16f;
    }
}
