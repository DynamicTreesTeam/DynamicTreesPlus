package com.ferreusveritas.dynamictreesplus.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.FruitGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;

public class CactusFruitGenFeature extends FruitGenFeature {

    public CactusFruitGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(FRUIT, QUANTITY, FRUITING_RADIUS, PLACE_CHANCE);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return new GenFeatureConfiguration(this)
                .with(FRUIT, Fruit.NULL)
                .with(QUANTITY, 2)
                .with(FRUITING_RADIUS, 4)
                .with(PLACE_CHANCE, 0.6f);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return species.hasFruit(configuration.get(FRUIT));
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.endPoints().isEmpty()) {
            int qty = configuration.get(QUANTITY);
            qty *= context.fruitProductionFactor();
            for (int i = 0; i < qty; i++) {
                final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));
                this.placeDuringWorldGen(configuration, context.species(), context.world(), context.pos().above(),
                        endPoint, context.bounds(), context.seasonValue());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() != 0) return false;

        final IWorld world = context.world();
        final BlockState blockState = world.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        final Fruit fruit = configuration.get(FRUIT);

        if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final BlockPos rootPos = context.pos();
            final float fruitingFactor = fruit.seasonalFruitProductionFactor(context.worldContext(), rootPos);

            if (fruitingFactor > fruit.getMinProductionFactor() && fruitingFactor > world.getRandom().nextFloat()) {
                final FindEndsNode endFinder = new FindEndsNode();
                TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
                final List<BlockPos> endPoints = endFinder.getEnds();
                int qty = configuration.get(QUANTITY);
                if (!endPoints.isEmpty()) {
                    for (int i = 0; i < qty; i++) {
                        final BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
                        this.place(configuration, context.species(), world, rootPos.above(), endPoint,
                                SeasonHelper.getSeasonValue(context.worldContext(), rootPos));
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected boolean shouldPlace(GenFeatureConfiguration configuration, IWorld world, BlockPos pos) {
        BlockState belowState = world.getBlockState(pos.below());
        return pos != BlockPos.ZERO
                && (CoordUtils.coordHashCode(pos, 0) & 6) == 0
                && world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE)
                && world.getBlockState(pos).getMaterial().isReplaceable()
                && !(belowState.getBlock() instanceof CactusBranchBlock && belowState.getValue(CactusBranchBlock.TRUNK_TYPE) != CactusBranchBlock.CactusThickness.BRANCH);
    }

    @Override
    protected void place(GenFeatureConfiguration configuration, Species species, IWorld world, BlockPos treePos,
                         BlockPos branchPos, Float seasonValue) {
        final BlockPos fruitPos = branchPos.above();
        if (shouldPlace(configuration, world, fruitPos)) {
            configuration.get(FRUIT).place(world, fruitPos, seasonValue);
        }
    }

    @Override
    protected void placeDuringWorldGen(GenFeatureConfiguration configuration, Species species, IWorld world,
                                       BlockPos treePos, BlockPos branchPos, SafeChunkBounds bounds,
                                       Float seasonValue) {
        final BlockPos fruitPos = branchPos.above();
        if (shouldPlaceDuringWorldGen(configuration, world, fruitPos)) {
            configuration.get(FRUIT).placeDuringWorldGen(world, fruitPos, seasonValue);
        }
    }

}
