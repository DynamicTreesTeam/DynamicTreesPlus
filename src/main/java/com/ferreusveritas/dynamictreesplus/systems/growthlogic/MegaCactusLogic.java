package com.ferreusveritas.dynamictreesplus.systems.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MegaCactusLogic extends CactusLogic {

    private static final int STOP_BRANCHING_HEIGHT = 5;
    private static final int MAX_HEIGHT = 7;

    public MegaCactusLogic(final ResourceLocation registryName) {
        super(registryName, 6);
    }

    @Override
    public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
        Direction originDir = signal.dir.getOpposite();

        int height = pos.getY() - signal.rootPos.getY();

        if (height >= MAX_HEIGHT && world.random.nextFloat() < 0.8f){
            signal.energy = 0;
            return new int[]{0,0,0,0,0,0};
        }

        if (height > STOP_BRANCHING_HEIGHT){
            //When above a certain height, all branches should grow straight up
            return new int[]{0,1,0,0,0,0};
        }

        //Alter probability map for direction change
        probMap[0] = 0; //Down is always disallowed for cactus
        probMap[1] = (int) (species.getUpProbability() + signal.rootPos.distSqr(pos.getX(), signal.rootPos.getY(), pos.getZ(), true) * 0.8);
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = world.getBlockState(pos.above()).getBlock() instanceof CactusBranchBlock && signal.energy > 1 ? 3 : 0;
        if (signal.dir != Direction.UP) probMap[signal.dir.ordinal()] = 0; //Disable the current direction, unless that direction is up
        probMap[originDir.ordinal()] = 0; //Disable the direction we came from

        return probMap;
    }

}
