package com.ferreusveritas.dynamictreesplus.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;

public class DTPDropCreators {

    public static final DropCreator CACTUS_SEEDS = new CactusSeedDropCreator(DynamicTreesPlus.resLoc("cactus_seeds"));

    public static void register(final Registry<DropCreator> registry) {
        registry.registerAll(CACTUS_SEEDS);
    }

}
