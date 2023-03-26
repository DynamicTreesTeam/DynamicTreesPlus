package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.deserialisation.EnumDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.PropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.RegistryEntryDeserialiser;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.tree.CactusSpecies;
import com.google.gson.JsonElement;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JsonRegistries {

    @SubscribeEvent
    public static void registerLoadAppliers(final ApplierRegistryEvent.Load<Species, JsonElement> event) {
        registerCactusLoadApplier(event.getAppliers());
    }

    @SubscribeEvent
    public static void registerReloadAppliers(final ApplierRegistryEvent.Reload<Species, JsonElement> event) {
        registerCactusReloadApplier(event.getAppliers());
    }

    @SubscribeEvent
    public static void registerAppliers(final ApplierRegistryEvent.GatherData<Species, JsonElement> event) {
        registerCactusReloadApplier(event.getAppliers());
    }

    public static void registerCactusLoadApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("is_seed_edible", CactusSpecies.class, Boolean.class,
                CactusSpecies::setSeedEdible);
    }

    public static void registerCactusReloadApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("cactus_thickness_logic", CactusSpecies.class, CactusThicknessLogic.class, CactusSpecies::setThicknessLogic);
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
