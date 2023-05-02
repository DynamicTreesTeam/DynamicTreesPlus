package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits;

import com.ferreusveritas.dynamictrees.api.configuration.Configurable;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurableRegistry;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MushroomShapeKit extends ConfigurableRegistryEntry<MushroomShapeKit, MushroomShapeConfiguration> implements Configurable {

    public static final ConfigurationProperty<Integer> MAX_CAP_AGE =
            ConfigurationProperty.integer("max_cap_age");

    public static final MushroomShapeKit NULL = new MushroomShapeKit(DynamicTreesPlus.location("null")) {
        @Override
        public void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {}
        @Override
        public void clearMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {}
        @Override
        public List<BlockPos> getShapeCluster(MushroomShapeConfiguration configuration, MushroomCapContext context) {return null;}

        @Override
        public int getMaxCapAge(MushroomShapeConfiguration configuration) {return 0;}
    };

    public static final ConfigurableRegistry<MushroomShapeKit, MushroomShapeConfiguration> REGISTRY =
            new ConfigurableRegistry<>(MushroomShapeKit.class, NULL, MushroomShapeConfiguration.TEMPLATES);

    public MushroomShapeKit(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override @NotNull
    protected MushroomShapeConfiguration createDefaultConfiguration() {
        return new MushroomShapeConfiguration(this);
    }

    @Override
    protected void registerProperties() {}

    public abstract void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context);
    public abstract void clearMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context);
    public abstract List<BlockPos> getShapeCluster(MushroomShapeConfiguration configuration, MushroomCapContext context);

    public abstract int getMaxCapAge(MushroomShapeConfiguration configuration);

}
