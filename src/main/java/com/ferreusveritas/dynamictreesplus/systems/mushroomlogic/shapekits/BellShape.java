package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BellShape extends MushroomShapeKit {

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
    public static final ConfigurationProperty<Integer> POINTED_TIP_AGE =
            ConfigurationProperty.integer("pointed_tip_max_age");

    public BellShape(ResourceLocation registryName) {
        super(registryName);
    }

    @Override @NotNull
    public MushroomShapeConfiguration getDefaultConfiguration() {
        return this.defaultConfiguration
                .with(CHANCE_TO_AGE, 0.75f)
                .with(MAX_CAP_AGE, 6)
                .with(CURVE_POWER, 3)
                .with(CURVE_HEIGHT_OFFSET, 0f)
                .with(MIN_AGE_CURVE_FACTOR, 2f)
                .with(MAX_AGE_CURVE_FACTOR, 0.5f)
                .with(CURVE_FACTOR_VARIATION, 0.1f)
                .with(POINTED_TIP_AGE, 0);
    }

    @Override
    protected void registerProperties() {
        this.register(CHANCE_TO_AGE, MAX_CAP_AGE, CURVE_POWER, CURVE_HEIGHT_OFFSET, MIN_AGE_CURVE_FACTOR, MAX_AGE_CURVE_FACTOR, CURVE_FACTOR_VARIATION, POINTED_TIP_AGE);
    }

    @Override
    public List<BlockPos> getShapeCluster(MushroomShapeConfiguration configuration, MushroomCapContext context){
        return placeRing(configuration, context, context.age(), ringAction.GET);
    }

    @Override
    public int getMaxCapAge(MushroomShapeConfiguration configuration) {
        return configuration.get(MAX_CAP_AGE);
    }

    @Override
    public float getChanceToAge(MushroomShapeConfiguration configuration) {
        return configuration.get(CHANCE_TO_AGE);
    }

    @Override
    public void generateMushroomCap(MushroomShapeConfiguration configuration, MushroomCapContext context) {
        placeRing(configuration, context, Math.min(context.age(), configuration.get(MAX_CAP_AGE)), ringAction.PLACE);
    }

    @Override
    public void clearMushroomCap (MushroomShapeConfiguration configuration, MushroomCapContext context){
        placeRing(configuration, context, context.age(),ringAction.CLEAR);
    }

    enum ringAction {
        PLACE,
        CLEAR,
        GET
    }

    private List<BlockPos> placeRing (MushroomShapeConfiguration configuration, MushroomCapContext context, int age, ringAction action){
        DynamicCapCenterBlock centerBlock = context.species().getCapProperties().getDynamicCapCenterBlock().orElse(null);
        List<BlockPos> ringPositions = new LinkedList<>();
        if (centerBlock == null) return ringPositions;

        float height_offset = configuration.get(CURVE_HEIGHT_OFFSET);
        int power = configuration.get(CURVE_POWER);

        float fac = calculateFactor(configuration, context);

        int y = 0;
        int radius = 1;
        for (int i=1; i<=age; i++){
            int nextY = fac == 0 ? 0 : (int)Math.floor(Math.pow(fac * radius, power) - height_offset);

            boolean moveY = i == 1 ? (age <= configuration.get(POINTED_TIP_AGE)) : nextY != y;
            if (moveY) y+= (int)Math.signum(fac);

            BlockPos pos = context.pos().below(y);
            if (action == ringAction.CLEAR)
                centerBlock.clearRing(context.level(), pos, radius);
            else if (action == ringAction.PLACE){
                // if the ring failed to generate then don't bother with the next rings
                if (!centerBlock.placeRing(context.level(), pos, radius, i, moveY, fac < 0 && i < age)) break;
            }
            else if (action == ringAction.GET)
                ringPositions.addAll(centerBlock.getRing(context.level(), pos, radius));

            if (i >= nextY) radius++;
        }
        ringPositions.add(context.pos());
        return ringPositions;
    }

    private float calculateFactor (MushroomShapeConfiguration configuration, MushroomCapContext context){
        HugeMushroomSpecies species = context.species();
        CapProperties properties = species.getCapProperties();
        int age = context.age();
        if (age == 0) return 0;
        float factorMin = configuration.get(MIN_AGE_CURVE_FACTOR);
        float factorMax = configuration.get(MAX_AGE_CURVE_FACTOR);
        float factorVariation = configuration.get(CURVE_FACTOR_VARIATION);

        float rand = (CoordUtils.coordHashCode(new BlockPos(context.pos().getX(), 0, context.pos().getZ()), 2) / (float)0xFFFF);
        float var = (rand*factorVariation*2) - factorVariation;

        return (((float)age)/(properties.getMaxAge(species))) * (factorMax - factorMin) + factorMin + var;
    }

}
