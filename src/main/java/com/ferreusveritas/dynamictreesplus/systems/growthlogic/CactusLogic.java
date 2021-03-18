package com.ferreusveritas.dynamictreesplus.systems.growthlogic;

import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CactusLogic extends GrowthLogicKit {

    private final int mod;

    public CactusLogic(final ResourceLocation registryName, final int mod) {
        super(registryName);
        this.mod = mod;
    }

    @Override
    public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
        return new int[] {0,1,0,0,0,0};
    }

    @Override
    public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
        return newDir;
    }

    @Override
    public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
        return CactusSpecies.getEnergy(world, pos, species, signalEnergy, this.mod);
    }

}
