package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context;

import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MushroomCapContext extends PositionalSpeciesContext {

    private final int currentAge;

    public MushroomCapContext(Level level, BlockPos pos, Species species, int currentAge) {
        super(level, pos, species);
        this.currentAge = currentAge;
    }

    public int getCurrentAge() {
        return currentAge;
    }
}
