package com.minecart.createaddon.recipes;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.recipes.compressing.CompressingRecipe;
import com.minecart.createaddon.recipes.sieving.SieveRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 1.20.1 Forge port: uses {@link RegistryObject} (no DeferredHolder), {@link Container}
 * (no RecipeInput), and {@link ProcessingRecipeSerializer} (no StandardProcessingRecipe.Serializer).
 */
public enum ModRecipes implements IRecipeTypeInfo {

    COMPRESSING(CompressingRecipe::new),
    SIEVING(SieveRecipe::new);

    public final ResourceLocation id;
    public final Supplier<RecipeSerializer<?>> serializerSupplier;
    private final RegistryObject<RecipeSerializer<?>> serializerObject;
    @Nullable
    private final RegistryObject<RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    <T extends ProcessingRecipe<?>> ModRecipes(ProcessingRecipeBuilder.ProcessingRecipeFactory<T> processingFactory) {
        this(() -> new ProcessingRecipeSerializer<>(processingFactory));
    }

    ModRecipes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = name().toLowerCase(Locale.ROOT);
        this.id = CreateAddon.modLoc(name);
        this.serializerSupplier = serializerSupplier;

        this.serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);

        this.typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
        this.type = typeObject;
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER.register(modEventBus);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RecipeType<?>> T getType() {
        return (T) type.get();
    }

    /**
     * 1.20.1: {@code RecipeManager.getRecipeFor(...)} returns {@code Optional<R>} directly
     * (no {@code RecipeHolder} wrapper).
     */
    @SuppressWarnings("unchecked")
    public <C extends Container, R extends Recipe<C>> Optional<R> find(C inv, Level world) {
        RecipeType<R> recipeType = (RecipeType<R>) type.get();
        return world.getRecipeManager().getRecipeFor(recipeType, inv, world);
    }

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
                DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateAddon.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER =
                DeferredRegister.create(Registries.RECIPE_TYPE, CreateAddon.MODID);
    }
}
