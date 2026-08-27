package com.dtteam.dynamictreesplus.worldgen.canceller;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

import java.util.stream.Stream;


public class MushroomFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> mushroomFeatureConfigClass;

    public MushroomFeatureCanceller(final Identifier registryName, final Class<T> mushroomFeatureConfigClass) {
        super(registryName);
        this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature, final BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configuredFeature.feature());

        if (featureRegistryName == null) {
            return false;
        }

        // Mushrooms come in RandomBooleanFeatureConfiguration or RandomFeatureConfiguration
        if (configuredFeature.config() instanceof RandomFeatureConfiguration randomFeatureConfig) {
            return randomFeatureContainsConfigClass(randomFeatureConfig.features.stream().map(w -> w.feature.value().feature().value()))
                    && featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
        }

        if (configuredFeature.config() instanceof RandomBooleanFeatureConfiguration randomBoolConfig) {
            return randomFeatureContainsConfigClass(randomBoolConfig.getSubFeatures().map(Holder::value))
                    && featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
        }

        //If it's not in a random feature config it may be alone.
        return this.mushroomFeatureConfigClass.isInstance(configuredFeature.config());
    }

    private boolean randomFeatureContainsConfigClass(Stream<ConfiguredFeature<?, ?>> randomFeatureConfig) {
        Stream<FeatureConfiguration> stream = randomFeatureConfig.map(ConfiguredFeature::config);
        return stream.anyMatch(this.mushroomFeatureConfigClass::isInstance);
    }

}