package com.ferreusveritas.dynamictreesplus.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CactusClonesGenFeature extends GenFeature {

    public static final ConfigurationProperty<Float> CHANCE_ON_GROW = ConfigurationProperty.floatProperty("chance_on_grow");
    public static final ConfigurationProperty<CactusBranchBlock.CactusThickness> TRUNK_TYPE = ConfigurationProperty.property("trunk_type", CactusBranchBlock.CactusThickness.class);

    public CactusClonesGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(CHANCE_ON_GROW, TRUNK_TYPE);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(CHANCE_ON_GROW, 0.3f).with(TRUNK_TYPE, CactusBranchBlock.CactusThickness.BRANCH);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        return this.tryToPlaceClones(context.world(), context.pos(), context.species(), true, context.bounds());
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        return context.random().nextFloat() < configuration.get(CHANCE_ON_GROW) &&
                this.tryToPlaceClones(context.world(), context.pos(), context.species(), false, SafeChunkBounds.ANY);
    }

    private boolean tryToPlaceClones (LevelAccessor world, BlockPos rootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds){
        if (world == null || areCactiAround(world, rootPos.above(), safeBounds)) return false;
        int clones = 3 + world.getRandom().nextInt(5); //between 3 and 8 clones
        List<CoordUtils.Surround> validDirs = new LinkedList<>(Arrays.asList(CoordUtils.Surround.values()));
        boolean clonePlaced = false;
        for (int i=0; i < clones; i++){
            CoordUtils.Surround selectedDir = validDirs.get(world.getRandom().nextInt(validDirs.size()));
            if (placeCloneAtLocation(world, rootPos.offset(selectedDir.getOffset()), species, worldgen, safeBounds))
                clonePlaced = true;
            validDirs.remove(selectedDir);
        }
        return clonePlaced;
    }

    private boolean areCactiAround(LevelAccessor world, BlockPos rootPos, SafeChunkBounds safeBounds){
        for (CoordUtils.Surround dir : CoordUtils.Surround.values()){
            for (int i=-1; i <= 1; i++){
                BlockPos offsetPos = rootPos.offset(dir.getOffset()).above(i);
                if (safeBounds.inBounds(offsetPos, false) && world.getBlockState(offsetPos).getBlock() instanceof BranchBlock){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeCloneAtLocation (LevelAccessor world, BlockPos cloneRootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds){
        for (int i=1; i >= -1; i--){
            BlockPos offsetRootPos = cloneRootPos.above(i);
            if (safeBounds.inBounds(offsetRootPos, false) && species.isAcceptableSoil(world.getBlockState(offsetRootPos),true)){

                if (worldgen) {
                    if (world instanceof WorldGenRegion)
                        species.generate(((WorldGenRegion)world).getLevel(), world, offsetRootPos, world.getNoiseBiome(offsetRootPos.getX(), offsetRootPos.getY(), offsetRootPos.getZ()).value(), world.getRandom(), 2, safeBounds);
                } else if (world instanceof Level) {
                    species.transitionToTree((Level) world, offsetRootPos.above());
                }
                return true;
            }
        }
        return false;
    }
}
