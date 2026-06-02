package com.dtteam.dynamictreesplus.systems.featuregen;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;

public class DynamicTreesPlusGenFeatures {

    public static final GenFeature CACTUS_CLONES = new CactusClonesGenFeature(DynamicTreesPlus.location("cactus_clones"));
    public static final GenFeature CACTUS_FRUIT = new CactusFruitGenFeature(DynamicTreesPlus.location("cactus_fruit"));

    public static void registerGenFeatures(final Registry<GenFeature> registry){
        registry.registerAll(CACTUS_CLONES, CACTUS_FRUIT);
    }

}
