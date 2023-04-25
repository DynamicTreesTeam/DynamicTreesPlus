package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.api.configuration.Configuration;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;

public final class MushroomShapeConfiguration extends Configuration<MushroomShapeConfiguration, MushroomShapeKit> {

//    public static final TemplateRegistry<MushroomShapeConfiguration> TEMPLATES = new TemplateRegistry<>();

    public MushroomShapeConfiguration(MushroomShapeKit configurable) {
        super(configurable);
    }

    @Override
    public MushroomShapeConfiguration copy() {
        final MushroomShapeConfiguration duplicateShapeKit = new MushroomShapeConfiguration(this.configurable);
        duplicateShapeKit.properties.putAll(this.properties);
        return duplicateShapeKit;
    }

//    /**
//     * Invokes {@link GrowthLogicKit#selectNewDirection(MushroomShapeConfiguration, DirectionSelectionContext)} for this
//     * configured kit's growth logic kit.
//     *
//     * @param context the context
//     * @return the direction for the signal to turn to
//     * @see GrowthLogicKit#selectNewDirection(MushroomShapeConfiguration, DirectionSelectionContext)
//     */
//    public Direction selectNewDirection(DirectionSelectionContext context) {
//        return this.configurable.selectNewDirection(this, context);
//    }
//
//    /**
//     * Invokes {@link GrowthLogicKit#populateDirectionProbabilityMap(MushroomShapeConfiguration,
//     * DirectionManipulationContext)} for this configured kit's growth logic kit.
//     *
//     * @param context the context
//     * @return the direction for the signal to turn to
//     * @see GrowthLogicKit#populateDirectionProbabilityMap(MushroomShapeConfiguration, DirectionManipulationContext)
//     */
//    public int[] populateDirectionProbabilityMap(DirectionManipulationContext context) {
//        return this.configurable.populateDirectionProbabilityMap(this, context);
//    }
//
//    /**
//     * Invokes {@link GrowthLogicKit#getEnergy(MushroomShapeConfiguration, PositionalSpeciesContext)} for this configured
//     * kit's growth logic kit.
//     *
//     * @param context the context
//     * @return the direction for the signal to turn to
//     * @see GrowthLogicKit#getEnergy(MushroomShapeConfiguration, PositionalSpeciesContext)
//     */
//    public float getEnergy(PositionalSpeciesContext context) {
//        return this.configurable.getEnergy(this, context);
//    }
//
//    /**
//     * Invokes {@link GrowthLogicKit#getLowestBranchHeight(MushroomShapeConfiguration, PositionalSpeciesContext)} for this
//     * configured kit's growth logic kit.
//     *
//     * @param context the context
//     * @return the direction for the signal to turn to
//     * @see GrowthLogicKit#getLowestBranchHeight(MushroomShapeConfiguration, PositionalSpeciesContext)
//     */
//    public int getLowestBranchHeight(PositionalSpeciesContext context) {
//        return this.configurable.getLowestBranchHeight(this, context);
//    }

    public static MushroomShapeConfiguration getDefault() {
        return MushroomShapeKit.DEFAULT.getDefaultConfiguration();
    }

    public int getDefaultDistance (){
        return this.configurable.getDefaultDistance();
    }

    public SimpleVoxmap getShapeCluster (){
        return this.configurable.getShapeCluster(this);
    }

    public void generateMushroomCap(MushroomCapContext context){
        this.configurable.generateMushroomCap(this, context);
    }

}
