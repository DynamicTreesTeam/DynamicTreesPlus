package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import com.dtteam.dynamictreesplus.init.DTPConfigs;
import com.dtteam.dynamictreesplus.items.FoodSeed;
import com.dtteam.dynamictreesplus.resources.DTPShapes;
import com.dtteam.dynamictreesplus.systems.growthlogic.DTPGrowthLogicKits;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CactusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(CactusSpecies::new);

    private CactusThicknessLogic thicknessLogic;
    private boolean isSeedEdible = false;

    public CactusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    @Override
    public Species setPreReloadDefaults() {
        this.setPreferredClimate(ClimateZoneType.ARID);
        return this.setSaplingShape(DTPShapes.MEDIUM_CACTUS_SAPLING_SHAPE)
                .setSaplingSound(SoundType.WOOL)
                .setDefaultGrowingParameters()
                .setGrowthLogicKit(DTPGrowthLogicKits.STRAIGHT_LOGIC);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (this.thicknessLogic == null)
        // Try to get the logic kit for the registry name.
        {
            this.thicknessLogic = CactusThicknessLogic.REGISTRY.get(this.getRegistryName());
        }

        return super.setPostReloadDefaults();
    }

    public void setThicknessLogic(CactusThicknessLogic thicknessLogic) {
        this.thicknessLogic = thicknessLogic;
    }

    public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level level, BlockPos pos, GrowSignal signal,
                                                                        CactusBranchBlock.CactusThickness currentThickness) {
        return this.thicknessLogic.thicknessAfterGrowthSignal(level, pos, signal, currentThickness);
    }

    public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor level, BlockPos pos, boolean isLast) {
        return this.thicknessLogic.thicknessForBranchPlaced(level, pos, isLast);
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(SoilHelper.SAND_LIKE);
    }

    @Override
    public JoCode getJoCode(String joCodeString) {
        return new JoCodeCactus(joCodeString);
    }

    @Override
    public boolean handleRot(LevelAccessor level, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int fertility, boolean worldGen) {
        return false;
    }

    @Override
    protected boolean transitionToTree(Level level, BlockPos pos, Family family) {
        this.placeRootyDirtBlock(level, pos.below(), 15); // Set to fully fertilized rooty sand underneath.
        family.getBranch().ifPresent(branch -> {
            level.setBlockAndUpdate(pos, branch.defaultBlockState().setValue(CactusBranchBlock.TRUNK_TYPE, this.thicknessForBranchPlaced(level, pos, false))); // Set to a single branch
        });
        return true;
    }

    @Override
    public boolean canBoneMealTree() {
        return DTPConfigs.CAN_BONE_MEAL_CACTUS.get();
    }

    private static class JoCodeCactus extends JoCode {

        public JoCodeCactus(String code) {
            super(code);
        }

        @Override
        public void generate(DynamicTreeGenerationContext context) {
            LevelAccessor level = context.level();
            BlockPos.MutableBlockPos rootPos = context.rootPos();
            BlockState initialDirtState = level.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
            context.species().placeRootyDirtBlock(level, rootPos, 0); // Set to unfertilized rooty dirt

            // A Tree generation boundary radius is at least 2 and at most 8
            int radius = Mth.clamp(context.radius(), 2, 8);
            BlockPos treePos = rootPos.above();

            // Create tree
            setFacing(context.facing());
            generateFork(level, context.species(), 0, rootPos, false);

            // Fix branch thicknesses and map out leaf locations
            BranchBlock branch = TreeHelper.getBranch(level.getBlockState(treePos));
            if (branch != null) {//If a branch exists then the growth was successful
                FindEndsNode endFinder = new FindEndsNode(); // This is responsible for gathering a list of branch end points
                MapSignal signal = new MapSignal(endFinder);
                branch.analyse(level.getBlockState(treePos), level, treePos, Direction.DOWN, signal);
                List<BlockPos> endPoints = endFinder.getEnds();

                // Allow for special decorations by the tree itself
                PostGenerationContext pgContext = new PostGenerationContext(context, endPoints, initialDirtState);
                context.species().postGeneration(pgContext);
                Services.EVENT.postSpeciesPostGenerationEvent(pgContext);
            } else { // The growth failed.. turn the soil back to what it was
                level.setBlock(rootPos, initialDirtState, careful ? 3 : 2);
            }
        }

        @Override
        public boolean setBlockForGeneration(LevelAccessor level, Species species, BlockPos pos, Direction dir,
                                             boolean careful, boolean isLast) {
            final Optional<BranchBlock> branch = species.getFamily().getBranch();
            if (!(species instanceof CactusSpecies) || branch.isEmpty()) {
                return false;
            }
            BlockState defaultBranchState = branch.get().defaultBlockState();
            BlockState replaceState = level.getBlockState(pos);
            boolean replace = (replaceState.isAir() || replaceState.is(BlockTags.LEAVES)) || replaceState.is(Blocks.GRASS_BLOCK) || replaceState.is(BlockTags.DIRT)
                    || replaceState.is(BlockTags.LOGS) || replaceState.is(BlockTags.SAPLINGS) || replaceState.is(Blocks.VINE);
            if (replace &&
                    (!careful || isClearOfNearbyBranches(level, pos, dir.getOpposite()))) {
                CactusBranchBlock.CactusThickness trunk =
                        ((CactusSpecies) species).thicknessForBranchPlaced(level, pos, isLast);
                return !level.setBlock(pos, defaultBranchState.setValue(CactusBranchBlock.TRUNK_TYPE, trunk)
                        .setValue(CactusBranchBlock.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
            }
            return true;
        }

    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTreesPlus.location("block/smart_model/" + this.thicknessLogic.getRegistryName().getPath() + "_cactus");
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        ResourceLocation sideLoc = this.getFamily().getTexturePath(Family.BRANCH).orElse(ResourceLocationUtils.suffix(barkTextureLocation, "_side"));
        ResourceLocation topLoc = this.getFamily().getTexturePath(Family.BRANCH_TOP).orElse(ResourceLocationUtils.suffix(barkTextureLocation, "_top"));
        ResourceLocation botLoc = this.getFamily().getTexturePath(CactusFamily.BRANCH_BOTTOM).orElse(ResourceLocationUtils.suffix(barkTextureLocation, "_bottom"));
        textureConsumer.accept("side", sideLoc);
        textureConsumer.accept("top", topLoc);
        textureConsumer.accept("bottom", botLoc);
    }

    @Override
    public boolean shouldGenerateVoluntaryDrops() {
        return false;
    }

    @Override
    public Species generateSeed() {
        return !this.shouldGenerateSeed() || this.seed != null ? this :
                this.setSeed(RegistryHandler.addItem(getSeedName(), this::createSeedItem));
    }

    public Seed createSeedItem(){
        return isSeedEdible ? new FoodSeed(this) : new Seed(this);
    }

    public void setSeedEdible (boolean edible){
        this.isSeedEdible = edible;
    }

}
