package com.dtteam.dynamictreesplus.systems.featuregen;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.CoordUtils;
import com.dtteam.dynamictrees.util.LevelContext;
import com.dtteam.dynamictrees.util.SafeChunkBounds;
import com.dtteam.dynamictrees.worldgen.GenerationContext;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
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
        return this.tryToPlaceClones(context.levelContext(), context.pos(), context.species(), true, context.bounds());
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        return context.random().nextFloat() < configuration.get(CHANCE_ON_GROW) &&
                this.tryToPlaceClones(context.levelContext(), context.pos(), context.species(), false, SafeChunkBounds.ANY);
    }

    private boolean tryToPlaceClones(LevelContext levelContext, BlockPos rootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds) {
        LevelAccessor level = levelContext.accessor();
        if (level == null || areCactiAround(level, rootPos.above(), safeBounds)) return false;
        int clones = 3 + level.getRandom().nextInt(5); //between 3 and 8 clones
        List<CoordUtils.Surround> validDirs = new LinkedList<>(Arrays.asList(CoordUtils.Surround.values()));
        boolean clonePlaced = false;
        for (int i = 0; i < clones; i++) {
            CoordUtils.Surround selectedDir = validDirs.get(level.getRandom().nextInt(validDirs.size()));
            if (placeCloneAtLocation(levelContext, rootPos.offset(selectedDir.getOffset()), species, worldgen, safeBounds))
                clonePlaced = true;
            validDirs.remove(selectedDir);
        }
        return clonePlaced;
    }

    private boolean areCactiAround(LevelAccessor world, BlockPos rootPos, SafeChunkBounds safeBounds) {
        for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
            for (int i = -1; i <= 1; i++) {
                BlockPos offsetPos = rootPos.offset(dir.getOffset()).above(i);
                if (safeBounds.inBounds(offsetPos, false) && world.getBlockState(offsetPos).getBlock() instanceof BranchBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeCloneAtLocation(LevelContext levelContext, BlockPos cloneRootPos, Species species, boolean worldgen, SafeChunkBounds safeBounds) {
        LevelAccessor level = levelContext.accessor();
        for (int i = 1; i >= -1; i--) {
            BlockPos offsetRootPos = cloneRootPos.above(i);
            if (safeBounds.inBounds(offsetRootPos, false) && species.isAcceptableSoil(level.getBlockState(offsetRootPos))) {

                if (worldgen) {
                    if (level instanceof WorldGenRegion) {
                        species.generate(new GenerationContext(levelContext, species, offsetRootPos, offsetRootPos.mutable(), level.getNoiseBiome(offsetRootPos.getX(), offsetRootPos.getY(), offsetRootPos.getZ()), CoordUtils.getRandomDir(level.getRandom()), 2, safeBounds));
                    }
                } else if (level instanceof Level) {
                    species.transitionToTree((Level) level, offsetRootPos.above());
                }
                return true;
            }
        }
        return false;
    }
}
