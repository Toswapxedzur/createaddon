package com.minecart.createaddon.jei.category.sequencedAssembly;

import com.minecart.createaddon.jei.category.animations.AnimatedBigPress;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 1.20.1 port: identical to 1.21.1. The {@link SequencedAssemblySubCategory} contract
 * ({@code SequencedAssemblySubCategory(int width)} + {@code draw(SequencedRecipe<?>, GuiGraphics, double, double, int)})
 * exists in both Create 6.0.8 and 6.0.9.
 */
public class AssemblyCompressing extends SequencedAssemblySubCategory {

    private final AnimatedBigPress press;

    public AssemblyCompressing() {
        super(25);
        press = new AnimatedBigPress(80);
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack ms = graphics.pose();
        press.offset = index;
        ms.pushPose();
        ms.translate(-5, 50, 0);
        ms.scale(.6f, .6f, .6f);
        press.draw(graphics, getWidth() / 2, 0);
        ms.popPose();
    }
}
