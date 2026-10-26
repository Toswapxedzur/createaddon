package com.minecart.createaddon.jei.category;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.jei.category.animations.AnimatedBigPress;
import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class CompressingCategory extends CreateRecipeCategory<CompressingRecipe> {
    public static final ResourceLocation UID = CreateAddon.modLoc("compressing");
    public static final RecipeType<CompressingRecipe> TYPE = RecipeType.create(CreateAddon.MODID, "compressing", CompressingRecipe.class);
    public static final CreateRecipeCategory<CompressingRecipe> INFO = new CreateRecipeCategoryBuilder<CompressingRecipe>(CompressingRecipe.class)
            .addTypedRecipes(ModRecipes.COMPRESSING)
            .catalyst(()->ModBlocks.BIGPRESS.asItem())
            .itemIcon(ModBlocks.BIGPRESS.asItem())
            .emptyBackground(177, 70)
            .build("compressing", CompressingCategory::new);

    private final Map<Integer, AnimatedBigPress> animationCache = new HashMap<>();

    public CompressingCategory(Info<CompressingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompressingRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 27, 51)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ProcessingOutput> results = recipe.getRollableResults();
        int i = 0;
        for (ProcessingOutput output : results) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131 + 19 * i, 50)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            i++;
        }
    }

    @Override
    public void draw(CompressingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 41);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);

        int duration = recipe.getProcessingDuration();
        AnimatedBigPress press = animationCache.computeIfAbsent(duration, AnimatedBigPress::new);
        press.draw(graphics, getBackground().getWidth() / 2 - 17, 22);

        drawProcessingTime(graphics, recipe);
    }

    private void drawProcessingTime(GuiGraphics graphics, CompressingRecipe recipe) {
        int durationTicks = recipe.getProcessingDuration();
        if (durationTicks > 0) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            MutableComponent timeString = Component.translatable("gui.jei.category.smelting.time.seconds", durationTicks / 20);
            int width = font.width(timeString);
            graphics.drawString(font, timeString, getBackground().getWidth() - width - 5, getBackground().getHeight() - 10, 0xFF888888, false);
        }
    }
}
