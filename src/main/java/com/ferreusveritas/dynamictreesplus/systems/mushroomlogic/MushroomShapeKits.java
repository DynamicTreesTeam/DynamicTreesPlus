package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;

public class MushroomShapeKits {

    public static void register(final Registry<MushroomShapeKit> registry) {
        registry.registerAll(BELL);
    }

    public static final MushroomShapeKit BELL = new MushroomShapeKit(DynamicTreesPlus.location("bell")) {
        @Override
        public int getDefaultDistance() {
            return 0;
        }

        @Override
        public SimpleVoxmap getShapeCluster() {
            return MushroomShapeClusters.NULL_MAP;
        }

    };


}
