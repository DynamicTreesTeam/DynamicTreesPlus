package com.ferreusveritas.dynamictreesplus.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;

public class DynamicTreesPlusGenFeatures {

    public static final GenFeature CACTUS_CLONES = new CactusClonesGenFeature(DynamicTreesPlus.resLoc("cactus_clones"));
    public static final GenFeature CACTUS_FRUIT = new CactusFruitGenFeature(DynamicTreesPlus.resLoc("cactus_fruit"));

    public static void registerGenFeatures(final RegistryEvent<GenFeature> event){
        event.getRegistry().registerAll(CACTUS_CLONES, CACTUS_FRUIT);
    }

}
