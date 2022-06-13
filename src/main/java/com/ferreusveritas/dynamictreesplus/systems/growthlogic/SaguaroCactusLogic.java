package com.ferreusveritas.dynamictreesplus.systems.growthlogic;

import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class SaguaroCactusLogic extends CactusLogic {

    public SaguaroCactusLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = context.probMap();
        final GrowSignal signal = context.signal();
        final Direction originDir = signal.dir.getOpposite();

        // Alter probability map for direction change.
        probMap[0] = 0; // Down is always disallowed for the cacti.
        probMap[1] = signal.delta.getX() % 2 == 0 || signal.delta.getZ() % 2 == 0 ? context.species().getUpProbability() : 0;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = signal.isInTrunk() && (signal.energy > 1) ? 1 : 0;

        if (signal.dir != Direction.UP)
            probMap[signal.dir.ordinal()] = 0; // Disable the current direction, unless that direction is up.

        probMap[originDir.ordinal()] = 0; // Disable the direction we came from.

        return probMap;
    }

}
