package com.dtteam.dynamictreesplus.event;

import com.dtteam.dynamictrees.api.DynamicTreesAddonEntrypoint;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.registry.FabricRegistryHandler;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.resources.CapPropertiesResourceLoader;
import com.dtteam.dynamictreesplus.resources.DTPJsonDeserializers;

public class DTPAddonEntrypoint implements DynamicTreesAddonEntrypoint {

    @Override
    public void onDynamicTreesPreSetup() {
        FabricRegistryHandler.setup(DynamicTreesPlus.MOD_ID);

        ModRegistryEntrypointHandler.registerGrowthLogic();
        ModRegistryEntrypointHandler.registerCactusThicknessLogic();
        ModRegistryEntrypointHandler.registerGenFeature();
        ModRegistryEntrypointHandler.registerFruitType();
        ModRegistryEntrypointHandler.registerFamilyType();
        ModRegistryEntrypointHandler.registerSpeciesType();
        ModRegistryEntrypointHandler.onFeatureCancellerRegistry();
        ModRegistryEntrypointHandler.onMushroomShapeKitRegistry();

        DTPJsonDeserializers.register();
    }

    @Override
    public void onAddResourceLoaders(TreeResourceManager resourceManager) {
        resourceManager.addLoader(CapPropertiesResourceLoader.CAP_PROPERTIES_LOADER);
        resourceManager.addLoader(CapPropertiesResourceLoader.MUSHROOM_SHAPE_KIT_TEMPLATE_LOADER);
    }

    @Override
    public <O, I> void onRegisterStagedApplier(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier) {
        JsonRegistriesEntrypointHandler.onRegisterStagedApplier(stage, appliers, identifier);
    }



}
