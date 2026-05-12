package com.minecart.createaddon.jei.category;

import com.minecart.createaddon.jei.category.animations.AnimatedSieve;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * 1.20.1 port: JEI category for {@link SieveRecipe}. Layout matches the 1.21.1 source —
 * input on the left, animated sieve in the middle, long arrow, then a column-major grid
 * of chance-weighted outputs on the right.
 */
@ParametersAreNonnullByDefault
public class SievingCategory extends CreateRecipeCategory<SieveRecipe> {
    public static final int BG_WIDTH = 177;
    public static final int BG_HEIGHT = 75;

    private static final int INPUT_X = 16;
    private static final int INPUT_Y = 28;
    private static final int OUTPUT_X = 130;
    private static final int OUTPUT_Y = 4;
    private static final int OUTPUT_STEP = 19;
    private static final int OUTPUT_ROWS = 3;

    private final AnimatedSieve sieve = new AnimatedSieve();

    public SievingCategory(Info<SieveRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SieveRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ProcessingOutput> results = recipe.getRollableResults();
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            int col = i / OUTPUT_ROWS;
            int row = i % OUTPUT_ROWS;
            builder.addSlot(RecipeIngredientRole.OUTPUT,
                            OUTPUT_X + col * OUTPUT_STEP,
                            OUTPUT_Y + row * OUTPUT_STEP)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(SieveRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 34, 47);
        AllGuiTextures.JEI_ARROW.render(graphics, 80, 30);

        sieve.draw(graphics, 44, 44);

        drawProcessingTime(graphics, recipe);
    }

    private void drawProcessingTime(GuiGraphics graphics, SieveRecipe recipe) {
        int durationTicks = recipe.getProcessingDuration();
        if (durationTicks <= 0)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        MutableComponent text =
                Component.translatable("gui.jei.category.smelting.time.seconds", durationTicks / 20);
        int width = font.width(text);
        graphics.drawString(font, text,
                getBackground().getWidth() - width - 5,
                getBackground().getHeight() - 10,
                0xFF888888, false);
    }
}
