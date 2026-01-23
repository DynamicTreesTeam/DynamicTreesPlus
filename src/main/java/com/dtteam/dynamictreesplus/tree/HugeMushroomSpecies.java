package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.CommonVoxelShapes;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SpeciesBlockEntity;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.cell.LeafClusters;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostRotContext;
import com.dtteam.dynamictrees.systems.nodemapper.CollectorNode;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.Optionals;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.block.mushroom.DynamicCapBlock;
import com.dtteam.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.dtteam.dynamictreesplus.init.DTPConfigs;
import com.dtteam.dynamictreesplus.init.DTPRegistries;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.dtteam.dynamictreesplus.systems.nodemapper.MushroomInflatorNode;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class HugeMushroomSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultMushroomType(HugeMushroomSpecies::new);

    public static TypedRegistry.EntryType<Species> createDefaultMushroomType(final Function3<ResourceLocation, Family, CapProperties, Species> constructor) {
        return TypedRegistry.newType(createDefaultMushroomCodec(constructor));
    }

    public static Codec<Species> createDefaultMushroomCodec(final Function3<ResourceLocation, Family, CapProperties, Species> constructor) {
        return RecordCodecBuilder.create(instance -> instance
                .group(
                        ResourceLocation.CODEC.fieldOf(TypedRegistry.RESOURCE_LOCATION.toString())
                                .forGetter(Species::getRegistryName),
                        Family.REGISTRY.getGetterCodec().fieldOf("family")
                                .forGetter(Species::getFamily),
                        CapProperties.REGISTRY.getGetterCodec().optionalFieldOf("cap_properties", CapProperties.NULL)
                                .forGetter(species -> species instanceof HugeMushroomSpecies ? ((HugeMushroomSpecies)species).getCapProperties() : CapProperties.NULL)
                )
                .apply(instance, constructor));
    }

    protected CapProperties capProperties = CapProperties.NULL;
    protected MushroomShapeConfiguration mushroomShapeKit;
    protected boolean acceptAnySoil = true;

    protected int maxLightForPlanting = 12;

    public HugeMushroomSpecies(ResourceLocation name, Family family, CapProperties capProperties) {
        this(name, family, MushroomShapeConfiguration.getDefault(), capProperties);
    }

    public HugeMushroomSpecies(ResourceLocation name, Family family, final MushroomShapeConfiguration shapeKit, CapProperties capProperties) {
        super(name, family, LeavesProperties.NULL);
        this.setCapProperties(capProperties.isValid() ? capProperties : (family instanceof HugeMushroomFamily ? ((HugeMushroomFamily)family).getCommonCap() : CapProperties.NULL));
        this.mushroomShapeKit = shapeKit;
    }

    @Override
    public Species setPreReloadDefaults() {
        this.setPreferredClimate(ClimateZoneType.ARID);
        return this.setSaplingShape(CommonVoxelShapes.ROUND_MUSHROOM)
                .setDefaultGrowingParameters()
                .setCanSaplingGrowNaturally(false)
                .setGrowthLogicKit(DTPRegistries.STRAIGHT_LOGIC);
    }

    public void setAcceptAnySoil(boolean acceptAnySoil) {
        this.acceptAnySoil = acceptAnySoil;
    }

    public void setMaxLightForPlanting(int maxLightForPlanting) {
        this.maxLightForPlanting = maxLightForPlanting;
    }

    public int getMaxLightForPlanting() {
        return maxLightForPlanting;
    }

    @Override
    protected boolean transitionToTree(Level level, BlockPos pos, Family family) {
        // Set to a single branch with 1 radius.
        family.getBranch().ifPresent(branch -> branch.setRadius(level, pos, family.getPrimaryThickness(), null));
        // Place a single leaf block on top.
        level.setBlockAndUpdate(pos.above(), getCapProperties().getDynamicCapState(true, 0));
        // Set to fully fertilized rooty dirt underneath.
        placeRootyDirtBlock(level, pos.below(), 15);

        if (doesRequireTileEntity(level, pos)) {
            SpeciesBlockEntity speciesBlockEntity = DTRegistries.SPECIES_BLOCK_ENTITY.get().create(pos.below(), level.getBlockState(pos.below()));
            assert speciesBlockEntity != null;
            level.setBlockEntity(speciesBlockEntity);
            speciesBlockEntity.setSpecies(this);
        }

        return true;
    }

    @Override
    public LogsAndSticks getLogsAndSticks(NetVolumeNode.Volume volume, boolean silkTouch, int fortuneLevel) {
        if (silkTouch) {
            return super.getLogsAndSticks(volume, true, fortuneLevel);
        }
        return new LogsAndSticks(new LinkedList<>(), 0);
    }

    ///////////////////////////////////////////
    // CAP
    ///////////////////////////////////////////

    public Species setCapProperties(CapProperties capProperties) {
        this.capProperties = capProperties;
        capProperties.setFamily(getFamily());
        return this;
    }

    public CapProperties getCapProperties() {
        return capProperties;
    }

    /**
     * Gets the {@link MushroomShapeKit}, which is for leaves automata.
     *
     * @return The configured {@link MushroomShapeConfiguration} object.
     */
    public MushroomShapeConfiguration getMushroomShapeKit() {
        return mushroomShapeKit;
    }

    public void setMushroomShapeConfiguration(MushroomShapeConfiguration mushroomShapeKit) {
        this.mushroomShapeKit = mushroomShapeKit;
    }

    public Optional<DynamicCapCenterBlock> getCapCenterBlock() {
        return this.capProperties.getDynamicCapCenterBlock();
    }

    public Optional<DynamicCapBlock> getCapBlock() {
        return this.capProperties.getDynamicCapBlock();
    }

    public Optional<Block> getPrimitiveCap() {
        return Optionals.ofBlock(this.capProperties.getPrimitiveCap().getBlock());
    }

    @Override
    public List<TagKey<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<TagKey<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTrees.location("block/smartmodel/mushroom_" + (this.getSaplingShape() == CommonVoxelShapes.FLAT_MUSHROOM ? "flat" : "round"));
    }

    public boolean shouldReplaceSaplingWhenPlaced(BlockState originalSapling) {
        if (DTPConfigs.REPLACE_MUSHROOM_SAPLING_ON_PLACEMENT.get())
            return super.shouldReplaceSaplingWhenPlaced(originalSapling);
        return false;
    }

    @Override
    public boolean shouldReplaceSaplingWhenGrown(BlockState originalSapling) {
        if (DTPConfigs.REPLACE_MUSHROOM_SAPLING_ON_GROWTH.get())
            return super.shouldReplaceSaplingWhenGrown(originalSapling);
        return false;
    }

    @Override
    public boolean isAcceptableSoil(LevelReader level, BlockPos pos, BlockState soilBlockState) {
        return (SoilHelper.isSoilAcceptable(soilBlockState, soilTypeFlags) || level.getRawBrightness(pos.above(), 0) <= getMaxLightForPlanting())
                && super.isAcceptableSoil(level, pos, soilBlockState);
    }

    @Override
    public boolean isAcceptableSoil(BlockState soilBlockState) {
        if (acceptAnySoil){
            Block soilBlock = soilBlockState.getBlock();
            return (soilBlock instanceof SoilBlock || SoilHelper.isSoilRegistered(soilBlock)) && !isWater(soilBlockState);
        }
        return super.isAcceptableSoil(soilBlockState);
    }

    public boolean isAcceptableSoilForWorldgen(LevelAccessor level, BlockPos pos, BlockState soilBlockState) {
        if (acceptAnySoil){
            Block soilBlock = soilBlockState.getBlock();
            return (soilBlock instanceof SoilBlock || SoilHelper.isSoilRegistered(soilBlock)) && !isWater(soilBlockState);
        }
        return super.isAcceptableSoilForWorldgen(level, pos, soilBlockState);
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        final ResourceLocation capLocation = ResourceLocationUtils.surround(this.getRegistryName(), "block/", "_cap");
        textureConsumer.accept("stem", barkTextureLocation);
        textureConsumer.accept("cap", capLocation);
    }

    public float getChanceToAge() {
        return mushroomShapeKit.getChanceToAge();
    }

    ///////////////////////////////////////////
    // RENDER
    ///////////////////////////////////////////

    @Override
    public boolean leavesAreSolid (){
        return true;
    }

    @Override
    public boolean canEncodeLeavesBlocks (BlockPos pos, BlockState state, Block block, BranchDestructionData data){
        return capProperties.isPartOfCap(state);
    }

    @Override
    public int encodeLeavesPos(BlockPos pos, BlockState state, Block block, BranchDestructionData data) {
        return (state.getBlock() instanceof DynamicCapCenterBlock ? 0 : state.getValue(DynamicCapBlock.DISTANCE) << 24) | BranchDestructionData.encodeRelBlockPos(pos);
    }

    @Override
    public int encodeLeavesBlocks(BlockPos pos, BlockState state, Block block, BranchDestructionData data) {
        //first bit will be 0 for cap blocks and 1 for the center cap block. The other bits encode direction or age, respectively.
        if (block instanceof DynamicCapBlock){
            boolean[] dirValues = DynamicCapBlock.getDirectionValues(state);
            int code = 0x0;
            for (int i=0; i<6; i++){
                if (dirValues[i])
                    code |= 0x1 << i + 1;
            }
            return code;
        } else if (block instanceof DynamicCapCenterBlock){
            int age = state.getValue(DynamicCapCenterBlock.AGE);
            int code = 0x1;
            code |= age << 1;
            return code;
        }
        return 0;
    }

    @Nullable
    public HashMap<BlockPos, BlockState> getFellingLeavesClusters(final BranchDestructionData destructionData) {
        HashMap<BlockPos, BlockState> map = new HashMap<>();

        for (int j=0; j<destructionData.getNumLeaves(); j++){
            int posCoded = destructionData.destroyedLeaves[j];
            int blockCoded = destructionData.destroyedLeavesBlockIndex[j];

            boolean isCenter = (blockCoded & 0x1) == 1;
            BlockState state = capProperties.getDynamicCapState(isCenter);
            if (isCenter){
                int age = blockCoded >> 0x1;
                state = state.setValue(DynamicCapCenterBlock.AGE, age);
            } else {
                boolean[] sides = new boolean[6];
                for (int i=0; i<6; i++){
                    sides[i] = ((blockCoded >> i + 1) & 0x1) == 0x1;
                }
                state = DynamicCapBlock.setDirectionValues(state, sides);
            }

            BlockPos pos = BranchDestructionData.decodeRelPos(posCoded);
            map.put(pos, state);
        }

        return map;
    }

    @Override
    public float falloverParticleFlingMultiplier() {
        return 0;
    }

    ///////////////////////////////////////////
    // GENERATION
    ///////////////////////////////////////////

    public MushroomInflatorNode getNodeInflator(List<Pair<BlockPos, Integer>> capAges, int radius, BlockPos rootPos) {
        return new MushroomInflatorNode(this, capAges, radius, rootPos);
    }

    @Override
    public JoCode getJoCode(String joCodeString) {
        return new JoCode(joCodeString){

            public void generate(DynamicTreeGenerationContext context) {
                LevelAccessor level = context.level();
                if (!(context.species() instanceof HugeMushroomSpecies species)) return;
                int radius = context.radius();
                boolean worldGen = context.isWorldGen();

                this.setFacing(context.facing());
                final BlockPos rootPos = species.preGeneration(level, context.rootPos(), radius, context.facing(), context.isWorldGen(), this);

                if (rootPos == BlockPos.ZERO) {
                    return;
                }

                final BlockState initialDirtState = level.getBlockState(rootPos); // Save the initial state of the dirt in case this fails.
                species.placeRootyDirtBlock(level, rootPos, 0); // Set to unfertilized rooty dirt.

                // Make the tree branch structure.
                this.generateFork(level, species, 0, rootPos, false);

                // Establish a position for the bottom block of the trunk.
                final BlockPos treePos = rootPos.above();

                // Fix branch thicknesses and map out leaf locations.
                final BlockState treeState = level.getBlockState(treePos);
                final BranchBlock firstBranch = TreeHelper.getBranch(treeState);

                // If a branch doesn't exist the growth failed.. turn the soil back to what it was.
                if (firstBranch == null) {
                    level.setBlock(rootPos, initialDirtState, this.careful ? 3 : 2);
                    return;
                }
                // If a branch exists then the growth was successful.

                List<Pair<BlockPos, Integer>> capAges = new LinkedList<>();
                final MushroomInflatorNode inflator = species.getNodeInflator(capAges, radius, rootPos); //This is responsible for gathering a list of branch end points.
                final FindEndsNode endFinder = new FindEndsNode(); //
                final MapSignal signal = new MapSignal(inflator, endFinder); // The inflator signal will "paint" a temporary voxmap of all of the leaves and branches.
                signal.destroyLoopedNodes = this.careful;

                firstBranch.analyse(treeState, level, treePos, Direction.DOWN, signal);

                if (signal.foundRoot || signal.overflow) { // Something went terribly wrong.
                    this.tryGenerateAgain(context, worldGen, treePos, treeState, endFinder);
                    return;
                }

                final List<BlockPos> endPoints = endFinder.getEnds();

                generateMushroomCap(context, species, capAges);

                // Rot the unsupported branches.
                if (species.handleRot(level, endPoints, rootPos, treePos, 0, context.isWorldGen())) {
                    return; // The entire tree rotted away before it had a chance.
                }

                // Allow for special decorations by the tree itself.
                PostGenerationContext pgContext = new PostGenerationContext(context, endPoints, initialDirtState);
                species.postGeneration(pgContext);
                Services.EVENT.postSpeciesPostGenerationEvent(pgContext);

            }

            private void generateMushroomCap(DynamicTreeGenerationContext context, HugeMushroomSpecies species, List<Pair<BlockPos, Integer>> capAges){
                LevelAccessor level = context.level();
                for (Pair<BlockPos, Integer> posAge : capAges){
                    int age = Math.min(posAge.getB(), species.getMushroomShapeKit().getMaxCapAge());
                    BlockPos centerPos = posAge.getA();
                    level.setBlock(centerPos, capProperties.getDynamicCapState(true, age), 2);
                    species.getMushroomShapeKit().generateMushroomCap(new MushroomCapContext(level, centerPos, species, age));
                }
            }

            @Override
            protected void cleanupFrankentree(LevelAccessor level, BlockPos treePos, BlockState treeState, List<BlockPos> endPoints, boolean worldGen) {
                final Set<BlockPos> blocksToDestroy = new HashSet<>();
                final BranchBlock branch = TreeHelper.getBranch(treeState);
                final MapSignal signal = new MapSignal(new CollectorNode(blocksToDestroy));

                signal.destroyLoopedNodes = false;
                signal.trackVisited = true;

                assert branch != null;

                branch.analyse(treeState, level, treePos, null, signal);
                BranchBlock.destroyMode = DynamicTrees.DestroyMode.IGNORE;

                for (BlockPos pos : blocksToDestroy) {
                    final BlockState branchState = level.getBlockState(pos);
                    final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(branchState);

                    if (branchBlock.isEmpty()) continue;

                    int radius = branchBlock.get().getRadius(branchState);
                    final Family family = branchBlock.get().getFamily();
                    final Species species = family.getCommonSpecies();

                    if (family.getPrimaryThickness() == radius) {
                        species.getLeavesProperties().ifValid(leavesProperties -> {
                            final SimpleVoxmap leafCluster = leavesProperties.getCellKit().getLeafCluster();

                            if (leafCluster != LeafClusters.NULL_MAP) {
                                for (SimpleVoxmap.VoxmapCell cell : leafCluster.getAllNonZeroCells()) {
                                    final BlockPos delPos = pos.offset(cell.getPos());
                                    final BlockState leavesState = level.getBlockState(delPos);
                                    if (TreeHelper.isLeaves(leavesState)) {
                                        final DynamicLeavesBlock leavesBlock = (DynamicLeavesBlock) leavesState.getBlock();
                                        if (leavesProperties.getFamily() == leavesBlock.getLeavesProperties().getFamily()) {
                                            level.setBlock(delPos, Blocks.AIR.defaultBlockState(), 2);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }

                BranchBlock.destroyMode = DynamicTrees.DestroyMode.HARVEST;
            }

        };
    }

    @Override
    public boolean rot(LevelAccessor level, BlockPos pos, int neighborCount, int radius, int fertility, RandomSource random, boolean rapid, boolean growLeaves) {
        int maxBranchRotRadius = Services.CONFIG.getIntConfig(IConfigHelper.MAX_BRANCH_ROT_RADIUS);
        if (rapid || maxBranchRotRadius != 0 && radius <= maxBranchRotRadius) {
            BranchBlock branch = TreeHelper.getBranch(level.getBlockState(pos));
            if (branch != null) {
                branch.rot(level, pos);
            }
            this.postRot(new PostRotContext(level, pos, this, radius, neighborCount, fertility, rapid));
            return true;
        }

        return false;
    }

    ///////////////////////////////////////////
    // SOUND EFFECTS
    ///////////////////////////////////////////

    public SoundEvent getFallingTreeStartSound (float treeVolume, boolean hasLeaves){
        return DTRegistries.FALLING_TREE_FUNGUS_START.get();
    }

    public SoundEvent getFallingTreeEndSound (float treeVolume, boolean hasLeaves){
        return DTRegistries.FALLING_TREE_FUNGUS_END.get();
    }

    public SoundEvent getFallingBranchEndSound (float treeVolume, boolean hasLeaves, boolean fellOnWater){
        return  hasLeaves ? DTRegistries.FALLING_TREE_FUNGUS_SMALL_END.get() : DTRegistries.FALLING_TREE_SMALL_END_BARE.get();
    }

    public float getFallingTreePitch (float treeVolume){
        return 2f/(1+treeVolume*0.1f);
    }
}
