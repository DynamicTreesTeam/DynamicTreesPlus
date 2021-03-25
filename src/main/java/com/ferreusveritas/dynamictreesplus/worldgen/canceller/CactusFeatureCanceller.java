package com.ferreusveritas.dynamictreesplus.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.block.Block;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;

/**
 * This class cancels any features that have a config that extends {@link BlockClusterFeatureConfig} and that
 * has a block set within that class that extends the cactus block class given (by default {@link CactusBlock}).
 *
 * @author Harley O'Connor
 */
public class CactusFeatureCanceller<T extends Block> extends FeatureCanceller {

    private final Class<T> cactusBlockClass;

    public CactusFeatureCanceller(final ResourceLocation registryName, Class<T> cactusBlockClass) {
        super(registryName);
        this.cactusBlockClass = cactusBlockClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        IFeatureConfig featureConfig = configuredFeature.config;

        if (!(featureConfig instanceof DecoratedFeatureConfig))
            return false;

        featureConfig = ((DecoratedFeatureConfig) featureConfig).feature.get().config;

        if (!(featureConfig instanceof DecoratedFeatureConfig))
            return false;

        final ConfiguredFeature<?, ?> currentConfiguredFeature = ((DecoratedFeatureConfig) featureConfig).feature.get();
        final ResourceLocation featureResLoc = currentConfiguredFeature.feature.getRegistryName();
        featureConfig = currentConfiguredFeature.config;

        if (!(featureConfig instanceof BlockClusterFeatureConfig))
            return false;

        final BlockClusterFeatureConfig blockClusterFeatureConfig = ((BlockClusterFeatureConfig) featureConfig);
        final BlockStateProvider stateProvider = blockClusterFeatureConfig.stateProvider;

        if (!(stateProvider instanceof SimpleBlockStateProvider))
            return false;

        // SimpleBlockStateProvider does not use random or BlockPos in getBlockState, so giving null is safe.
        return this.cactusBlockClass.isInstance(stateProvider.getBlockState(null, null).getBlock())
                && featureResLoc != null && featureCancellations.shouldCancelNamespace(featureResLoc.getNamespace());
    }

}
