package com.dtteam.dynamictreesplus.worldgen.canceller;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * This class cancels any features that have a config that extends {@link VegetationPatchConfiguration} and that
 * has a block set within that class that extends the cactus block class given (by default {@link CactusBlock}).
 *
 * @author Harley O'Connor
 */
public class CactusFeatureCanceller<T extends Block> extends FeatureCanceller {

    private static final RandomSource PLACEHOLDER_RANDOM = RandomSource.create(0L);

    private final Class<T> cactusBlockClass;

    public CactusFeatureCanceller(final Identifier registryName, Class<T> cactusBlockClass) {
        super(registryName);
        this.cactusBlockClass = cactusBlockClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        Identifier featureResLoc = BuiltInRegistries.FEATURE.getKey(configuredFeature.feature());
        if (featureResLoc == null)
            return false;

        FeatureConfiguration featureConfig = configuredFeature.config();

        if (featureConfig instanceof VegetationPatchConfiguration randomPatchConfiguration) {
            PlacedFeature placedFeature = randomPatchConfiguration.vegetationFeature.value();
            featureConfig = placedFeature.feature().value().config();
        }

        if (!(featureConfig instanceof BlockColumnConfiguration blockColumnConfiguration) || !featureCancellations.shouldCancelNamespace(featureResLoc.getNamespace())) {
            return false;
        }

        for (BlockColumnConfiguration.Layer layer : blockColumnConfiguration.layers()) {

            if (layer.state() instanceof SimpleStateProvider ssp &&
                    this.cactusBlockClass.isInstance(ssp.getState(null, PLACEHOLDER_RANDOM, BlockPos.ZERO).getBlock())) {
                return true;
            }
        }

        return false;
    }
}
