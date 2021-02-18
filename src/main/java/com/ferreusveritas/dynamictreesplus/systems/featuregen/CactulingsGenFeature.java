package com.ferreusveritas.dynamictreesplus.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CactulingsGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

    public static final GenFeatureProperty<Float> CHANCE_ON_GROW = GenFeatureProperty.createFloatProperty("chance_on_grow");
    public static final GenFeatureProperty<CactusBranchBlock.CactusThickness> TRUNK_TYPE = GenFeatureProperty.createProperty("trunk_type", CactusBranchBlock.CactusThickness.class);

    public CactulingsGenFeature(ResourceLocation registryName) {
        super(registryName, CHANCE_ON_GROW, TRUNK_TYPE);
    }

    @Override
    protected ConfiguredGenFeature<?> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(CHANCE_ON_GROW, 0.3f).with(TRUNK_TYPE, CactusBranchBlock.CactusThickness.BRANCH);
    }

    @Override
    public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        //return tryToPlaceClones(worldFromIWorld(world), rootPos, species, true, safeBounds);
        return false;
    }

    @Override
    public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (world.rand.nextFloat() < configuredGenFeature.get(CHANCE_ON_GROW))
            return tryToPlaceClones(world, rootPos, species, false, SafeChunkBounds.ANY);
        return false;
    }

    private boolean areCactiAround (World world, BlockPos rootPos, SafeChunkBounds safeBounds){
        for (CoordUtils.Surround dir : CoordUtils.Surround.values()){
            for (int i=-1; i <= 1; i++){
                BlockPos offsetPos = rootPos.add(dir.getOffset()).up(i);
                if (safeBounds.inBounds(offsetPos, false) && world.getBlockState(offsetPos).getBlock() instanceof BranchBlock){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryToPlaceClones (World world, BlockPos rootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds){
        if (world == null || areCactiAround(world, rootPos, safeBounds)) return false;
        int clones = 3 + world.rand.nextInt(5); //between 3 and 8 clones
        List<CoordUtils.Surround> validDirs = new LinkedList<>(Arrays.asList(CoordUtils.Surround.values()));
        boolean clonePlaced = false;
        for (int i=0; i < clones; i++){
            CoordUtils.Surround selectedDir = validDirs.get(world.rand.nextInt(validDirs.size()));
            if (placeCloneAtLocation(world, rootPos.add(selectedDir.getOffset()), species, worldgen, safeBounds))
                clonePlaced = true;
            validDirs.remove(selectedDir);
        }
        return clonePlaced;
    }

    //This just fetches a World instance from an IWorld instance, since IWorld cannot be used to create bees.
    private World worldFromIWorld (IWorld iWorld){
        if (iWorld instanceof WorldGenRegion){
            return  ((WorldGenRegion)iWorld).getWorld();
        } else if (iWorld instanceof World){
            return  (World)iWorld;
        }
        return null;
    }

    private boolean placeCloneAtLocation (World world, BlockPos cloneRootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds){
        for (int i=1; i >= -1; i--){
            BlockPos offsetRootPos = cloneRootPos.up(i);
            if (safeBounds.inBounds(offsetRootPos, false) && species.isAcceptableSoil(world.getBlockState(offsetRootPos))){

                if (worldgen) {
                    species.generate(world, world, offsetRootPos, world.getNoiseBiome(offsetRootPos.getX(), offsetRootPos.getY(), offsetRootPos.getZ()), world.rand, 2, safeBounds);
                } else {
                    species.transitionToTree(world, offsetRootPos.up());
                }
//                    int energy = (int) species.getEnergy(world, offsetRootPos);
//                    for (int e=0; e<energy-1; e++){
//                        species.generate(world, world, offsetRootPos, world.getNoiseBiome(offsetRootPos.getX(), offsetRootPos.getY(), offsetRootPos.getZ()), world.rand, 2, safeBounds);
//                        BlockPos branchOffsetPos = offsetRootPos.up(1+e);
//                        if (safeBounds.inBounds(branchOffsetPos, false) && world.isAirBlock(branchOffsetPos))
//                            world.setBlockState(branchOffsetPos, species.getFamily().getDynamicBranch().getDefaultState().with(CactusBranchBlock.ORIGIN, Direction.DOWN).with(CactusBranchBlock.TRUNK_TYPE, trunkType));
//                    }
                return true;
            }
        }
        return false;
    }
}
