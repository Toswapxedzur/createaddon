package com.minecart.createaddon.jei;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModBlocks;
import com.minecart.createaddon.jei.category.CompressingCategory;
import com.minecart.createaddon.jei.category.SievingCategory;
import com.minecart.createaddon.recipes.ModRecipes;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1.20.1 Forge JEI plugin port. Differences from the 1.21.1 NeoForge plugin:
 * <ul>
 *   <li>{@link CreateRecipeCategory.Info} is built inline (no public {@code Builder} in Create 6.0.8;
 *       the builder moved into a package-private {@code CreateJEI.CategoryBuilder}).</li>
 *   <li>Recipe lookups use {@code RecipeManager#getAllRecipesFor} which returns the raw recipe
 *       (no {@code RecipeHolder} wrapper in 1.20.1).</li>
 * </ul>
 */
@JeiPlugin
public class CreateAddonJeiPlugin implements IModPlugin {
    public static final ResourceLocation ID = CreateAddon.modLoc("jei_plugin");

    @Nullable
    private CompressingCategory compressingCategory;
    @Nullable
    private SievingCategory sievingCategory;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();

        compressingCategory = new CompressingCategory(buildInfo(
                "compressing",
                CompressingRecipe.class,
                gui.createBlankDrawable(177, 70),
                gui.createDrawableItemLike(ModBlocks.BIGPRESS.asItem())
        ));
        sievingCategory = new SievingCategory(buildInfo(
                "sieving",
                SieveRecipe.class,
                gui.createBlankDrawable(SievingCategory.BG_WIDTH, SievingCategory.BG_HEIGHT),
                gui.createDrawableItemLike(ModBlocks.MECHANICAL_SIEVE.asItem())
        ));

        registration.addRecipeCategories(compressingCategory);
        registration.addRecipeCategories(sievingCategory);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (compressingCategory != null) {
            registration.addRecipes(compressingCategory.getRecipeType(),
                    typedRecipes(ModRecipes.COMPRESSING.<net.minecraft.world.item.crafting.RecipeType<CompressingRecipe>>getType()));
        }
        if (sievingCategory != null) {
            registration.addRecipes(sievingCategory.getRecipeType(),
                    typedRecipes(ModRecipes.SIEVING.<net.minecraft.world.item.crafting.RecipeType<SieveRecipe>>getType()));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (compressingCategory != null) {
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIGPRESS.asItem()), compressingCategory.getRecipeType());
        }
        if (sievingCategory != null) {
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_SIEVE.asItem()), sievingCategory.getRecipeType());
        }
    }

    /**
     * 1.20.1: {@code AllGuiTextures.JEI_SLOT}-style backgrounds & icons must be wrapped into a
     * proper {@link IDrawable} since {@code CreateRecipeCategory.Info} expects {@code IDrawable}
     * for both. We use a JEI blank drawable for the background (matches 1.21.1's
     * {@code .emptyBackground(...)}) and JEI's {@code createDrawableItemLike} for the icon.
     */
    private <T extends Recipe<?>> CreateRecipeCategory.Info<T> buildInfo(String name, Class<? extends T> recipeClass,
                                                                        IDrawable background, IDrawable icon) {
        RecipeType<T> jeiType = new RecipeType<>(CreateAddon.modLoc(name), recipeClass);
        Component title = Component.translatable("createaddon.recipe." + name);
        // List<Supplier<? extends ItemStack>> — catalysts are also registered via
        // registerRecipeCatalysts for legacy display, but Info needs the same list.
        List<java.util.function.Supplier<? extends ItemStack>> catalysts = new ArrayList<>();
        if ("compressing".equals(name)) {
            catalysts.add(() -> new ItemStack(ModBlocks.BIGPRESS.asItem()));
        } else if ("sieving".equals(name)) {
            catalysts.add(() -> new ItemStack(ModBlocks.MECHANICAL_SIEVE.asItem()));
        }
        return new CreateRecipeCategory.Info<>(jeiType, title, background, icon,
                () -> Collections.emptyList(), catalysts);
    }

    /**
     * Fetch recipes via the client recipe manager for a given {@link net.minecraft.world.item.crafting.RecipeType}.
     * 1.20.1: returns raw recipes, no {@code RecipeHolder} wrapper.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Recipe<?>> List<T> typedRecipes(net.minecraft.world.item.crafting.RecipeType<T> type) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List recipes = level.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType) type);
        return (List<T>) recipes;
    }

    @SuppressWarnings("unused")
    private static IDrawable asDrawable(AllGuiTextures texture, IGuiHelper gui) {
        return gui.createBlankDrawable(texture.getWidth(), texture.getHeight());
    }
}
