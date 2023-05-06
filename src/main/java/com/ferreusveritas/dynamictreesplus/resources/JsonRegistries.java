package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.deserialisation.EnumDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.PropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.RegistryEntryDeserialiser;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.tree.CactusSpecies;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomFamily;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JsonRegistries {

    private static final Logger LOGGER = LogManager.getLogger();

    private static void logError(ResourceLocation name, String error, String registryName) {
        LOGGER.error("Error whilst loading type \"" + registryName + "\" with name \"" + name + "\": {}", error);
    }

    private static void logWarning(ResourceLocation name, String warning, String registryName) {
        LOGGER.warn("Warning whilst loading type \"" + registryName + "\" with name \"" + name + "\": {}", warning);
    }

    ///////////////////////////////////////////
    // FAMILY
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerFamilyCommonAppliers(final ApplierRegistryEvent.Common<Family, JsonElement> event) {
        registerMushroomCommonApplier(event.getAppliers());
    }
    @SubscribeEvent
    public static void registerFamilyDataAppliers(final ApplierRegistryEvent.GatherData<Family, JsonElement> event) {
        registerMushroomDataApplier(event.getAppliers());
    }
    public static void registerMushroomCommonApplier(PropertyAppliers<Family, JsonElement> appliers) {
        appliers.register("common_cap", HugeMushroomFamily.class, ResourceLocation.class,
                (family, registryName) -> {
                    final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
                    CapProperties.REGISTRY.runOnNextLock(CapProperties.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            family::setCommonCap,
                            () -> logWarning(family.getRegistryName(),
                                    "Could not set common cap for family with name \"" + family
                                            + "\" as cap \"" + processedRegName + "\" was not found.", Family.REGISTRY.getName())
                    ));
                });
    }
    public static void registerMushroomDataApplier(PropertyAppliers<Family, JsonElement> appliers) {
        appliers.register("outside_branch_texture", HugeMushroomFamily.class, ResourceLocation.class,
                HugeMushroomFamily::setOutsideBranchTexture)
                .register("inside_branch_texture", HugeMushroomFamily.class, ResourceLocation.class,
                HugeMushroomFamily::setInsideBranchTexture);
    }

    ///////////////////////////////////////////
    // SPECIES
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerSpeciesLoadAppliers(final ApplierRegistryEvent.Load<Species, JsonElement> event) {
        registerCactusLoadApplier(event.getAppliers());
    }
    @SubscribeEvent
    public static void registerSpeciesReloadAppliers(final ApplierRegistryEvent.Reload<Species, JsonElement> event) {
        registerMushroomReloadApplier(event.getAppliers());
        registerCactusReloadApplier(event.getAppliers());
    }
    @SubscribeEvent
    public static void registerSpeciesDataAppliers(final ApplierRegistryEvent.GatherData<Species, JsonElement> event) {
        registerCactusReloadApplier(event.getAppliers());
    }

    public static void registerCactusLoadApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("is_seed_edible", CactusSpecies.class, Boolean.class,
                CactusSpecies::setSeedEdible);
    }
    public static void registerMushroomReloadApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("cap_properties", HugeMushroomSpecies.class, CapProperties.class, HugeMushroomSpecies::setCapProperties)
                .register("mushroom_shape_kit", HugeMushroomSpecies.class, MushroomShapeConfiguration.class, HugeMushroomSpecies::setMushroomShapeConfiguration)
                .register("accept_any_soil", HugeMushroomSpecies.class, Boolean.class, HugeMushroomSpecies::setAcceptAnySoil)
                .register("max_light_for_planting", HugeMushroomSpecies.class, Integer.class, HugeMushroomSpecies::setMaxLightForPlanting);
    }
    public static void registerCactusReloadApplier(PropertyAppliers<Species, JsonElement> appliers) {
        appliers.register("cactus_thickness_logic", CactusSpecies.class, CactusThicknessLogic.class, CactusSpecies::setThicknessLogic);
    }

    ///////////////////////////////////////////
    // DESERIALIZERS
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerJsonDeserializers(final JsonDeserialisers.RegistryEvent event) {
        // Register cactus thickness logic kits and lock it.
        CactusThicknessLogic.REGISTRY.postRegistryEvent();
        MushroomShapeKit.REGISTRY.postRegistryEvent();
        CactusThicknessLogic.REGISTRY.lock();
        MushroomShapeKit.REGISTRY.lock();

        // Register getter for cactus thickness logic and mushroom shape logic.
        JsonDeserialisers.register(CactusThicknessLogic.class,
                new RegistryEntryDeserialiser<>(CactusThicknessLogic.REGISTRY));
        JsonDeserialisers.register(MushroomShapeKit.class,
                new RegistryEntryDeserialiser<>(MushroomShapeKit.REGISTRY));

        // Register getter for cactus thickness enum.
        JsonDeserialisers.register(CactusBranchBlock.CactusThickness.class,
                new EnumDeserialiser<>(CactusBranchBlock.CactusThickness.class));
    }

}
