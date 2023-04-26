package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context;

import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MushroomCapContext extends PositionalSpeciesContext {

    private final int age;
    private final HugeMushroomSpecies mushySpecies;

    public MushroomCapContext(Level level, BlockPos pos, HugeMushroomSpecies species, int age) {
        super(level, pos, species);
        this.age = age;
        this.mushySpecies = species;
    }

    public int getAge() {
        return age;
    }

    @Override
    public HugeMushroomSpecies species() {
        return mushySpecies;
    }

}
