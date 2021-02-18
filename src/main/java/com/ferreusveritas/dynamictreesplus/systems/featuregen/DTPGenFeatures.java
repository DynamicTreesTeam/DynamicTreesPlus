package com.ferreusveritas.dynamictreesplus.systems.featuregen;

import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class DTPGenFeatures {

    public static final GenFeature CACTULINGS = register(new CactulingsGenFeature(regName("cactulings")));

    private static ResourceLocation regName(String name) {
        return new ResourceLocation(DynamicTreesPlus.MOD_ID, name);
    }

    private static GenFeature register(GenFeature genFeature) {
        GenFeature.REGISTRY.register(genFeature);
        return genFeature;
    }

}
