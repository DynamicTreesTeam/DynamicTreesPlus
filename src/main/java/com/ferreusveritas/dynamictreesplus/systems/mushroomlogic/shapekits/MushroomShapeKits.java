package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;

public class MushroomShapeKits {

    public static final MushroomShapeKit BELL_MUSHROOM_SHAPE = new BellShape(DynamicTreesPlus.location("bell"));

    public static void register(final Registry<MushroomShapeKit> registry) {
        registry.register(BELL_MUSHROOM_SHAPE);
    }

}
