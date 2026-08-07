package com.minecart.createaddon.ponder;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * 1.20.1 port: Ponder 1.0.91 keeps the same {@link PonderPlugin} contract as 1.21.1
 * Ponder, so the scene/tag registration code is unchanged. The only required swap is
 * {@code RegisteredObjectsHelper::getKeyOrThrow} (1.21.1 catnip static method) →
 * {@code CatnipServices.REGISTRIES::getKeyOrThrow} (1.20.1 service interface).
 */
public class ModPonder implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateAddon.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // 1.20.1 Registrate's ItemProviderEntry has a single type parameter (the produced
        // ItemLike); 1.21.1 added a second one for the registrate context, hence the diff
        // from the source.
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(ModBlocks.NOTEBLOCK_ENCASED_SHAFT, ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL)
                .addStoryBoard(CreateAddon.modLoc("kinetic_noteblock"),
                        ModPonderScenes::encasedNoteblock,
                        AllCreatePonderTags.KINETIC_APPLIANCES);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper =
                helper.withKeyFunction(CatnipServices.REGISTRIES::getKeyOrThrow);

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(ModBlocks.NOTEBLOCK_ENCASED_SHAFT.asItem())
                .add(ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL.asItem());
    }
}
