package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.deserialisation.EnumDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.PropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.RegistryEntryDeserialiser;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import com.google.gson.JsonElement;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JsonRegistries {

    @SubscribeEvent
    public static void registerAppliers(final ApplierRegistryEvent.Reload<Species, JsonElement> event) {
        registerCactusThicknessApplier(event.getAppliers());
    }

    @SubscribeEvent
    public static void registerAppliers(final ApplierRegistryEvent.GatherData<Species, JsonElement> event) {
        registerCactusThicknessApplier(event.getAppliers());
    }

    public static void registerCactusThicknessApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("cactus_thickness_logic", CactusSpecies.class, CactusThicknessLogic.class,
                CactusSpecies::setThicknessLogic);
    }

    @SubscribeEvent
    public static void registerJsonDeserialisers(final JsonDeserialisers.RegistryEvent event) {
        // Register cactus thickness logic kits and lock it.
        CactusThicknessLogic.REGISTRY.postRegistryEvent();
        CactusThicknessLogic.REGISTRY.lock();

        // Register getter for cactus thickness logic.
        JsonDeserialisers.register(CactusThicknessLogic.class,
                new RegistryEntryDeserialiser<>(CactusThicknessLogic.REGISTRY));

        // Register getter for cactus thickness enum.
        JsonDeserialisers.register(CactusBranchBlock.CactusThickness.class,
                new EnumDeserialiser<>(CactusBranchBlock.CactusThickness.class));
    }

}
