package com.ferreusveritas.dynamictreesplus.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.registry.IRegistry;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;

public class DTPDropCreators {

    public static final DropCreator CACTUS_SEEDS = new CactusSeedDropCreator(DynamicTreesPlus.resLoc("cactus_seeds"));

    public static void register(final IRegistry<DropCreator> registry) {
        registry.registerAll(CACTUS_SEEDS);
    }

}
