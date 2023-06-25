package com.ferreusveritas.dynamictreesplus.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomCapDisc;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import oshi.util.tuples.Pair;

import java.util.List;

public class MushroomInflatorNode implements NodeInspector {

    private static final int minRadiusHeightDivider = 3;
    private float radius;
    private BlockPos last;
    private BlockPos highestTrunkBlock;
    private final List<Pair<BlockPos, Integer>> capAges;
    private final int generationRadius;
    private int lastCapBranchRadius;
    private final BlockPos rootPos;

    HugeMushroomSpecies species;

    public MushroomInflatorNode(HugeMushroomSpecies species, List<Pair<BlockPos, Integer>> capAges, int genRadius, BlockPos root) {
        this.species = species;
        last = BlockPos.ZERO;
        highestTrunkBlock = null;
        this.capAges = capAges;
        this.generationRadius = genRadius;
        this.rootPos = root;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            radius = species.getFamily().getPrimaryThickness();
            //Store the last block to be part of the trunk
            if (highestTrunkBlock == null && !TreeHelper.isBranch(level.getBlockState(pos.above()))){
                highestTrunkBlock = pos;
            }
        }

        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BlockPos dist = pos.subtract(last);
        if (dist.getX() * dist.getX() + dist.getY() * dist.getY() + dist.getZ() * dist.getZ() != 1) {//This is actually the equation for distance squared. 1 squared is 1. Yay math.
            if (DynamicCapCenterBlock.canCapReplace(level.getBlockState(pos.above()))){
                int height = pos.subtract(rootPos).getY();
                int maxAge = Math.min(Math.min(Math.min(species.getCapProperties().getMaxAge(species), MushroomCapDisc.MAX_RADIUS), height), generationRadius);
                int minAge = Math.max(0, height / minRadiusHeightDivider);
                int capAge = minAge + CoordUtils.coordHashCode(new BlockPos(pos.getX(), 0, pos.getZ()),  3) % ((maxAge == minAge ? 0 : Math.abs(maxAge-minAge))+1);
                lastCapBranchRadius = Math.min(species.getFamily().getPrimaryThickness() + capAge, species.getMaxBranchRadius());
                radius = lastCapBranchRadius;
                capAges.add(new Pair<>(pos.above(), capAge));
            }
        }

        //Calculate Branch Thickness based on neighboring branches

        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            float areaAccum = radius * radius;//Start by accumulating the branch we just came from
            boolean isTwig = true;

            for (Direction dir : Direction.values()) {
                if (!dir.equals(fromDir)) {//Don't count where the signal originated from

                    BlockPos dPos = pos.relative(dir);

                    if (dPos.equals(last)) {//or the branch we just came back from
                        isTwig = false;//on the return journey if the block we just came from is a branch we are obviously not the endpoint(twig)
                        continue;
                    }

                    BlockState deltaBlockState = level.getBlockState(dPos);
                    TreePart treepart = TreeHelper.getTreePart(deltaBlockState);
                    if (branch.isSameTree(treepart)) {
                        int branchRadius = treepart.getRadius(deltaBlockState);
                        areaAccum += branchRadius * branchRadius;
                    }
                }
            }

            if (isTwig){
                branch.setRadius(level, pos, (int)radius, null);
            }
            else {
                //The new branch should be the square root of all of the sums of the areas of the branches coming into it.
                radius = (float) Math.sqrt(areaAccum) + (species.getTapering() * species.getWorldGenTaperingFactor());

                //Ensure the branch is never inflated past it's species maximum
                int maxRadius = species.getMaxBranchRadius();
                if (radius > maxRadius) {
                    radius = maxRadius;
                }

                if (highestTrunkBlock != null){
                    //Ensure branches dont grow over 1 block thick if it isnt in the trunk
                    int blockRadius = 8;
                    boolean isInTrunk = (pos.getX() == highestTrunkBlock.getX() && pos.getY() <= highestTrunkBlock.getY() && pos.getZ() == highestTrunkBlock.getZ());
                    if (radius > blockRadius && !isInTrunk){
                        radius = blockRadius;
                    }
                }

                //Ensure non-twig branches are a radius higher than
                float secondaryThickness = Math.min(lastCapBranchRadius+1, species.getMaxBranchRadius());
                if (radius < secondaryThickness) {
                    radius = secondaryThickness;
                }

                branch.setRadius(level, pos, (int) Math.floor(radius), null);
            }

            last = pos;

        }

        return false;
    }

}
