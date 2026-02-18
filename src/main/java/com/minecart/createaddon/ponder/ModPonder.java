package com.minecart.createaddon.ponder;

import com.minecart.createaddon.CreateAddon;
import com.minecart.createaddon.ModBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ModPonder implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateAddon.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(ModBlocks.NOTEBLOCK_ENCASED_SHAFT, ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL).addStoryBoard(CreateAddon.modLoc("kinetic_noteblock"), ModPonderScenes::encasedNoteblock, AllCreatePonderTags.KINETIC_APPLIANCES);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(ModBlocks.NOTEBLOCK_ENCASED_SHAFT)
                .add(ModBlocks.NOTEBLOCK_ENCASED_COGWHEEL);
    }
}
