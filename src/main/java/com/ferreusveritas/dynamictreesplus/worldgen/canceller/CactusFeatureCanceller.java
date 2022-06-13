package com.ferreusveritas.dynamictreesplus.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

import java.util.Random;

/**
 * This class cancels any features that have a config that extends {@link RandomPatchConfiguration} and that
 * has a block set within that class that extends the cactus block class given (by default {@link CactusBlock}).
 *
 * @author Harley O'Connor
 */
public class CactusFeatureCanceller<T extends Block> extends FeatureCanceller {

    private static final Random PLACEHOLDER_RAND = new Random();

    private final Class<T> cactusBlockClass;

    public CactusFeatureCanceller(final ResourceLocation registryName, Class<T> cactusBlockClass) {
        super(registryName);
        this.cactusBlockClass = cactusBlockClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        FeatureConfiguration featureConfig = configuredFeature.config();


//        final ConfiguredFeature<?, ?> currentConfiguredFeature =  featureConfig.getFeatures().findFirst().get();
//        final ResourceLocation featureResLoc = currentConfiguredFeature.feature().getRegistryName();
//        featureConfig = currentConfiguredFeature.config();
//
//        if (!(featureConfig instanceof RandomPatchConfiguration))
//            return false;
//
//        final RandomPatchConfiguration blockClusterFeatureConfig = ((RandomPatchConfiguration) featureConfig);
//        final BlockStateProvider stateProvider = blockClusterFeatureConfig.feature().value().;
        boolean isCactus = configuredFeature.feature() == VegetationFeatures.PATCH_CACTUS.value().feature();
//        if (!(stateProvider instanceof SimpleStateProvider))
//            return false;

        // SimpleBlockStateProvider does not use random or BlockPos in getBlockState, so giving null is safe.
        return isCactus;/*this.cactusBlockClass.isInstance(stateProvider.getState(PLACEHOLDER_RAND, BlockPos.ZERO).getBlock())
                && featureResLoc != null && featureCancellations.shouldCancelNamespace(featureResLoc.getNamespace());*/
    }

}
