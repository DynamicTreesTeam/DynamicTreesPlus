package com.ferreusveritas.dynamictreesplus.tree;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.nodemapper.FindEndsNode;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import com.ferreusveritas.dynamictreesplus.init.DTPRegistries;
import com.ferreusveritas.dynamictreesplus.items.FoodSeed;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.suffix;

public class CactusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(CactusSpecies::new);

    private CactusThicknessLogic thicknessLogic;
    private boolean isSeedEdible = false;

    public CactusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    @Override
    public Species setPreReloadDefaults() {
        this.setTransformable(false);
        return this.setSaplingShape(DTPRegistries.MEDIUM_CACTUS_SAPLING_SHAPE)
                .setSaplingSound(SoundType.WOOL)
                .setDefaultGrowingParameters()
                .envFactor(Tags.Biomes.IS_SNOWY, 0.25f)
                .envFactor(Tags.Biomes.IS_COLD, 0.5f)
                .envFactor(Tags.Biomes.IS_SANDY, 1.05f)
                .setGrowthLogicKit(DTPRegistries.STRAIGHT_LOGIC);
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
    public boolean isBiomePerfect(Holder<Biome> biome) {
        return this.perfectBiomes.size() > 0 ? super.isBiomePerfect(biome) :
                biome.is(Tags.Biomes.IS_DRY) && biome.is(Tags.Biomes.IS_SANDY);
    }

    @Override
    public boolean handleRot(LevelAccessor level, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife,
                             SafeChunkBounds safeBounds) {
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
        public void generate(GenerationContext context) {
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
                context.species().postGeneration(new PostGenerationContext(context, endPoints, initialDirtState));
                MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(level, context.species(), rootPos, endPoints, context.safeBounds(), initialDirtState));
            } else { // The growth failed.. turn the soil back to what it was
                level.setBlock(rootPos, initialDirtState, careful ? 3 : 2);
            }
        }

        @Override
        public boolean setBlockForGeneration(LevelAccessor level, Species species, BlockPos pos, Direction dir,
                                             boolean careful, boolean isLast) {
            final Optional<BranchBlock> branch = species.getFamily().getBranch();
            if (!(species instanceof CactusSpecies) || !branch.isPresent()) {
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
        final ResourceLocation sideTextureLocation = suffix(barkTextureLocation, "_side");
        textureConsumer.accept("particle", sideTextureLocation);
        textureConsumer.accept("side", sideTextureLocation);
        textureConsumer.accept("top", suffix(barkTextureLocation, "_top"));
        textureConsumer.accept("bottom", suffix(barkTextureLocation, "_bottom"));
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
