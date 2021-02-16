package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.trees.Cactus;

public class DTPTrees {

    public static Cactus dynamicCactus;

    public static void setupTrees() {

        dynamicCactus = new Cactus();
        dynamicCactus.registerSpecies(Species.REGISTRY);

        //Registers a fake species for generating mushrooms
//        Species.REGISTRY.register(new Mushroom(true));
//        Species.REGISTRY.register(new Mushroom(false));

    }

}
