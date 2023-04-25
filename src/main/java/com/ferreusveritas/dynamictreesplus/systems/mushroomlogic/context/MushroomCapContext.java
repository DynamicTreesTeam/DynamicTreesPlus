package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context;

import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MushroomCapContext extends PositionalSpeciesContext {

    private final int currentAge;
    private final DynamicCapCenterBlock centerBlock;
    private final BlockPos rootPos;

    public MushroomCapContext(Level level, BlockPos pos, Species species, int currentAge, DynamicCapCenterBlock centerBlock, BlockPos rootPos) {
        super(level, pos, species);
        this.currentAge = currentAge;
        this.centerBlock = centerBlock;
        this.rootPos = rootPos;
    }

    public int getCurrentAge() {
        return currentAge;
    }

    public DynamicCapCenterBlock getCenterBlock() {
        return centerBlock;
    }

    public BlockPos getRootPos() {
        return rootPos;
    }
}
