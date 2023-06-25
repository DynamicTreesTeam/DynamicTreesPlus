package com.ferreusveritas.dynamictreesplus.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class cancels any features that have a config that extends {@link RandomPatchConfiguration} and that
 * has a block set within that class that extends the cactus block class given (by default {@link CactusBlock}).
 *
 * @author Harley O'Connor
 */
public class CactusFeatureCanceller<T extends Block> extends FeatureCanceller {

    private static final RandomSource PLACEHOLDER_RANDOM = RandomSource.create(0L);

    private final Class<T> cactusBlockClass;

    public CactusFeatureCanceller(final ResourceLocation registryName, Class<T> cactusBlockClass) {
        super(registryName);
        this.cactusBlockClass = cactusBlockClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        ResourceLocation featureResLoc = ForgeRegistries.FEATURES.getKey(configuredFeature.feature());
        if (featureResLoc == null)
            return false;

        FeatureConfiguration featureConfig = configuredFeature.config();

        if (featureConfig instanceof RandomPatchConfiguration randomPatchConfiguration) {
            PlacedFeature placedFeature = randomPatchConfiguration.feature().value();
            featureConfig = placedFeature.feature().value().config();
        }

        if (!(featureConfig instanceof BlockColumnConfiguration blockColumnConfiguration) || !featureCancellations.shouldCancelNamespace(featureResLoc.getNamespace())) {
            return false;
        }

        for (BlockColumnConfiguration.Layer layer : blockColumnConfiguration.layers()) {
            final BlockStateProvider stateProvider = layer.state();
            if (!(stateProvider instanceof SimpleStateProvider)) {
                continue;
            }

            // SimpleStateProvider does not use Random or BlockPos in getState, but we still provide non-null values just to be safe
            if (this.cactusBlockClass.isInstance(stateProvider.getState(PLACEHOLDER_RANDOM, BlockPos.ZERO).getBlock())) {
                return true;
            }
        }

        return false;
    }
}
