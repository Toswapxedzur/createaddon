package com.minecart.createaddon.jei.category;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.jei.category.animations.AnimatedSieve;
import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * JEI recipe category for {@link SieveRecipe}: input on the left, animated sieve in the
 * middle, a long arrow pointing right, and the chance-weighted outputs listed top-to-bottom
 * in a single column on the right. The processing duration in seconds is drawn at the
 * bottom-left of the background.
 */
@ParametersAreNonnullByDefault
public class SievingCategory extends CreateRecipeCategory<SieveRecipe> {
    public static final ResourceLocation UID = CreateAddon.modLoc("sieving");
    public static final RecipeType<SieveRecipe> TYPE =
            RecipeType.create(CreateAddon.MODID, "sieving", SieveRecipe.class);

    /**
     * Background dimensions matched to {@link com.minecart.createaddon.jei.category.CompressingCategory}.
     * Worst-case 9 outputs are tiled into a 3-row column-major grid so they still read
     * top-to-bottom while keeping the overall height comparable to the other categories.
     */
    private static final int BG_WIDTH = 177;
    private static final int BG_HEIGHT = 75;

    private static final int INPUT_X = 16;
    private static final int INPUT_Y = 28;
    /** Top-left of the column-major output grid. */
    private static final int OUTPUT_X = 130;
    private static final int OUTPUT_Y = 4;
    private static final int OUTPUT_STEP = 19;
    /** Max outputs per column before wrapping to a new column on the right. */
    private static final int OUTPUT_ROWS = 3;

    public static final CreateRecipeCategory<SieveRecipe> INFO =
            new CreateRecipeCategoryBuilder<SieveRecipe>(SieveRecipe.class)
                    .addTypedRecipes(ModRecipes.SIEVING)
                    .catalyst(() -> ModBlocks.MECHANICAL_SIEVE.asItem())
                    .itemIcon(ModBlocks.MECHANICAL_SIEVE.asItem())
                    .emptyBackground(BG_WIDTH, BG_HEIGHT)
                    .build("sieving", SievingCategory::new);

    /** Constant rotation, no per-recipe state needed → single shared instance. */
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
            // Column-major: fill each column top-to-bottom, then wrap to the next column on the right.
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
        // Bottom-right corner, per spec.
        int width = font.width(text);
        graphics.drawString(font, text,
                getBackground().getWidth() - width - 5,
                getBackground().getHeight() - 10,
                0xFF888888, false);
    }
}
