package com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits;

import com.dtteam.dynamictrees.api.configuration.Configurable;
import com.dtteam.dynamictrees.api.configuration.ConfigurableRegistry;
import com.dtteam.dynamictrees.api.configuration.ConfigurableRegistryEntry;
import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MushroomShapeKit extends ConfigurableRegistryEntry<MushroomShapeKit, MushroomShapeConfiguration> implements Configurable {

    public static final ConfigurationProperty<Integer> MAX_CAP_AGE =
            ConfigurationProperty.integer("max_cap_age");
    public static final ConfigurationProperty<Float> CHANCE_TO_AGE =
            ConfigurationProperty.floatProperty("chance_to_age");

    public static final MushroomShapeKit NULL = new MushroomShapeKit(DynamicTreesPlus.location("null")) {
        @Override
        public void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {}
        @Override
        public void clearMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {}
        @Override
        public List<BlockPos> getShapeCluster(MushroomShapeConfiguration configuration, MushroomCapContext context) {return null;}

        @Override
        public int getMaxCapAge(MushroomShapeConfiguration configuration) {return 0;}

        @Override
        public float getChanceToAge(MushroomShapeConfiguration configuration) {
            return 0;
        }
    };

    public static final ConfigurableRegistry<MushroomShapeKit, MushroomShapeConfiguration> REGISTRY =
            new ConfigurableRegistry<>(MushroomShapeKit.class, NULL, MushroomShapeConfiguration.TEMPLATES);

    public MushroomShapeKit(final Identifier registryName) {
        super(registryName);
    }

    @Override
    public Class<MushroomShapeKit> getRegistryType() {
        return MushroomShapeKit.class;
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
    public abstract float getChanceToAge(MushroomShapeConfiguration configuration);
}
