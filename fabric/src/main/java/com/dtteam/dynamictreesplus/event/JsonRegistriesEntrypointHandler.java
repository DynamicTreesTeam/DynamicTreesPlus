package com.dtteam.dynamictreesplus.event;

import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.dtteam.dynamictreesplus.tree.CactusSpecies;
import com.dtteam.dynamictreesplus.tree.HugeMushroomFamily;
import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class JsonRegistriesEntrypointHandler {

    public static <O, I> void onRegisterStagedApplier(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier){
        if (stage == StagedApplierResourceLoader.ApplierStage.COMMON){
            runIfValidType(Family.class, JsonRegistriesEntrypointHandler::registerMushroomCommonApplier, appliers);
        }
        if (stage == StagedApplierResourceLoader.ApplierStage.LOAD){
            runIfValidType(Species.class, JsonRegistriesEntrypointHandler::registerCactusLoadApplier, appliers);
        }
        if (stage == StagedApplierResourceLoader.ApplierStage.RELOAD){
            runIfValidType(Species.class, JsonRegistriesEntrypointHandler::registerMushroomReloadApplier, appliers);
            runIfValidType(Species.class, JsonRegistriesEntrypointHandler::registerCactusReloadApplier, appliers);
        }
        if (stage == StagedApplierResourceLoader.ApplierStage.GATHER_DATA){
            runIfValidType(Species.class, JsonRegistriesEntrypointHandler::registerCactusReloadApplier, appliers);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, O,I> void runIfValidType(Class<T> objectType, Consumer<PropertyAppliers<T, JsonElement>> consumer, PropertyAppliers<O, I> appliers){
        if (appliers.getInputType() == JsonElement.class && appliers.getObjectType() == objectType){
            consumer.accept((PropertyAppliers<T, JsonElement>)appliers);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static void logWarning(Identifier name, String warning, String registryName) {
        LOGGER.warn("Warning whilst loading type \"{}\" with name \"{}\": {}", registryName, name, warning);
    }

    public static void registerMushroomCommonApplier(PropertyAppliers<Family, JsonElement> appliers) {
        appliers.register("common_cap", HugeMushroomFamily.class, Identifier.class,
                (family, registryName) -> {
                    final Identifier processedRegName = IdentifierUtils.parseDTLocation(registryName);
                    CapProperties.REGISTRY.runOnNextLock(CapProperties.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            family::setCommonCap,
                            () -> logWarning(family.getRegistryName(),
                                    "Could not set common cap for family with name \"" + family
                                            + "\" as cap \"" + processedRegName + "\" was not found.", Family.REGISTRY.getName())
                    ));
                });
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
}
