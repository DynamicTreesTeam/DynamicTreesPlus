package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.api.configuration.Configurable;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class MushroomShapeKit extends ConfigurableRegistryEntry<MushroomShapeKit, MushroomShapeConfiguration> implements Configurable {

    public static final ConfigurationProperty<Integer> MAX_CAP_AGE =
            ConfigurationProperty.integer("max_cap_age");
    public static final ConfigurationProperty<Integer> CURVE_POWER =
            ConfigurationProperty.integer("curve_power");
    public static final ConfigurationProperty<Float> CURVE_HEIGHT_OFFSET =
            ConfigurationProperty.floatProperty("curve_height_offset");
    public static final ConfigurationProperty<Float> MIN_AGE_CURVE_FACTOR =
            ConfigurationProperty.floatProperty("min_age_curve_factor");
    public static final ConfigurationProperty<Float> MAX_AGE_CURVE_FACTOR =
            ConfigurationProperty.floatProperty("max_age_curve_factor");
    public static final ConfigurationProperty<Float> CURVE_FACTOR_VARIATION =
            ConfigurationProperty.floatProperty("curve_factor_variation");


    public static final MushroomShapeKit DEFAULT = new MushroomShapeKit(DynamicTreesPlus.location("default")) {
        @Override @NotNull
        public MushroomShapeConfiguration getDefaultConfiguration() {
            return this.defaultConfiguration
                    .with(MAX_CAP_AGE, 6)
                    .with(CURVE_POWER, 3)
                    .with(CURVE_HEIGHT_OFFSET, 0f)
                    .with(MIN_AGE_CURVE_FACTOR, 2f)
                    .with(MAX_AGE_CURVE_FACTOR, 0.5f)
                    .with(CURVE_FACTOR_VARIATION, 0.1f);
        }

        @Override
        protected void registerProperties() {
            this.register(MAX_CAP_AGE, CURVE_POWER, CURVE_HEIGHT_OFFSET, MIN_AGE_CURVE_FACTOR, MAX_AGE_CURVE_FACTOR, CURVE_FACTOR_VARIATION);
        }

        private float calculateFactor (MushroomShapeConfiguration configuration, MushroomCapContext context){
            HugeMushroomSpecies species = (HugeMushroomSpecies) context.species();
            CapProperties properties = species.getCapProperties();
            int age = context.getCurrentAge();
            if (age == 0) return 0;
            float factorMin = configuration.get(MIN_AGE_CURVE_FACTOR);
            float factorMax = configuration.get(MAX_AGE_CURVE_FACTOR);
            float factorVariation = configuration.get(CURVE_FACTOR_VARIATION);

            float rand = (CoordUtils.coordHashCode(context.getRootPos(), 2) / (float)0xFFFF);
            float var = (rand*factorVariation*2) - factorVariation;

            return (((float)age)/(properties.getMaxAge())) * (factorMax - factorMin) + factorMin + var;
        }

        @Override
        public void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {
            DynamicCapCenterBlock centerBlock = context.getCenterBlock();
            if (centerBlock == null) return;
            int age = Math.min(context.getCurrentAge(), configuration.get(MAX_CAP_AGE));

            float height_offset = configuration.get(CURVE_HEIGHT_OFFSET);
            int power = configuration.get(CURVE_POWER);

            float fac = calculateFactor(configuration, context);

            int y = 0;
            int radius = 1;
            for (int i=1; i<=age; i++){
                int nextY = fac == 0 ? 0 : (int)Math.floor(Math.pow(fac * radius, power) - height_offset);

                boolean moveY = nextY != y && i != 1;
                if (moveY) y+= (int)Math.signum(fac);

                centerBlock.placeRing(context.level(), context.pos().below(y), radius, i, moveY);

                if (i >= nextY) radius++;
            }
        }

        public void clearMushroomCap (MushroomShapeConfiguration configuration, MushroomCapContext context){
            DynamicCapCenterBlock centerBlock = context.getCenterBlock();
            if (centerBlock == null) return;
            int age = context.getCurrentAge();

            float height_offset = configuration.get(CURVE_HEIGHT_OFFSET);
            int power = configuration.get(CURVE_POWER);

            float fac = calculateFactor(configuration, context);

            int y = 0;
            int radius = 1;
            for (int i=1; i<=age; i++){
                int nextY = fac == 0 ? 0 : (int)Math.floor(Math.pow(fac * radius, power) - height_offset);

                if (nextY != y && i != 1) y+= (int)Math.signum(fac);

                centerBlock.clearRing(context.level(), context.pos().below(y), radius);

                if (i >= nextY) radius++;
            }
        }
    };

    public static final SimpleRegistry<MushroomShapeKit> REGISTRY = new SimpleRegistry<>(MushroomShapeKit.class, DEFAULT);

    public MushroomShapeKit(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override @NotNull
    protected MushroomShapeConfiguration createDefaultConfiguration() {
        return new MushroomShapeConfiguration(this);
    }

    @Override
    protected void registerProperties() {}

    public int getDefaultDistance() { return 1; }

    public SimpleVoxmap getShapeCluster(MushroomShapeConfiguration configuration){
        return MushroomShapeClusters.NULL_MAP;
    }

    public abstract void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context);
    public abstract void clearMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context);

}
