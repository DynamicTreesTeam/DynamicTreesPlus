package com.dtteam.dynamictreesplus.event;

import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusFruit;
import com.dtteam.dynamictreesplus.systems.featuregen.DynamicTreesPlusGenFeatures;
import com.dtteam.dynamictreesplus.systems.growthlogic.DTPGrowthLogicKits;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKits;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogicKits;
import com.dtteam.dynamictreesplus.tree.CactusFamily;
import com.dtteam.dynamictreesplus.tree.CactusSpecies;
import com.dtteam.dynamictreesplus.tree.HugeMushroomFamily;
import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import com.dtteam.dynamictreesplus.worldgen.canceller.CactusFeatureCanceller;
import com.dtteam.dynamictreesplus.worldgen.canceller.MushroomFeatureCanceller;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class ModRegistryEntrypointHandler {

    public static void registerGrowthLogic() {
        DTPGrowthLogicKits.register(GrowthLogicKit.REGISTRY);
    }

    public static void registerCactusThicknessLogic() {
        CactusThicknessLogic.REGISTRY.registerAll(CactusThicknessLogicKits.PILLAR, CactusThicknessLogicKits.PIPE, CactusThicknessLogicKits.SAGUARO, CactusThicknessLogicKits.MEGA);
    }

    public static void registerGenFeature() {
        DynamicTreesPlusGenFeatures.registerGenFeatures(GenFeature.REGISTRY);

    }
    public static void registerFruitType() {
        Fruit.REGISTRY.registerType(DynamicTreesPlus.location("cactus_fruit"), CactusFruit.TYPE);

    }

    public static void registerFamilyType() {
        Family.REGISTRY.registerType(DynamicTreesPlus.CACTUS, CactusFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTreesPlus.MUSHROOM, HugeMushroomFamily.TYPE);
    }

    public static void registerSpeciesType() {
        Species.REGISTRY.registerType(DynamicTreesPlus.CACTUS, CactusSpecies.TYPE);
        Species.REGISTRY.registerType(DynamicTreesPlus.MUSHROOM, HugeMushroomSpecies.TYPE);
    }

    public static void onFeatureCancellerRegistry() {
        FeatureCanceller.REGISTRY.registerAll(new CactusFeatureCanceller<>(DynamicTreesPlus.location("cactus"), CactusBlock.class));
        FeatureCanceller.REGISTRY.registerAll(new MushroomFeatureCanceller<>(DynamicTreesPlus.location("mushroom"), HugeMushroomFeatureConfiguration.class));
    }

    public static void onMushroomShapeKitRegistry() {
        MushroomShapeKits.register(MushroomShapeKit.REGISTRY);
    }


}
