package com.ferreusveritas.dynamictreesplus.tree;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.cell.LeafClusters;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.nodemapper.CollectorNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.FindEndsNode;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.*;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapBlock;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.ferreusveritas.dynamictreesplus.init.DTPRegistries;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.systems.nodemapper.MushroomInflatorNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.surround;

public class HugeMushroomSpecies extends Species {

    public static final Codec<Species> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString())
                            .forGetter(Species::getRegistryName),
                    Family.REGISTRY.getGetterCodec().fieldOf("family").forGetter(Species::getFamily),
                    CapProperties.REGISTRY.getGetterCodec().optionalFieldOf("cap_properties",
                            CapProperties.NULL).forGetter(species -> species instanceof HugeMushroomSpecies ? ((HugeMushroomSpecies)species).getCapProperties() : CapProperties.NULL))
            .apply(instance, HugeMushroomSpecies::new));

    public static final TypedRegistry.EntryType<Species> TYPE = TypedRegistry.newType(CODEC);

    protected CapProperties capProperties = CapProperties.NULL;
    protected boolean acceptAnySoil = true;
    protected int maxLightForPlanting = 12;

    public HugeMushroomSpecies(ResourceLocation name, Family family, CapProperties capProperties) {
        super(name, family, LeavesProperties.NULL);
        this.setCapProperties(capProperties.isValid() ? capProperties : (family instanceof HugeMushroomFamily ? ((HugeMushroomFamily)family).getCommonCap() : CapProperties.NULL));
    }

    @Override
    public Species setPreReloadDefaults() {
        this.setTransformable(false);
        return this.setSaplingShape(CommonVoxelShapes.ROUND_MUSHROOM)
                .setDefaultGrowingParameters()
                .envFactor(BiomeDictionary.Type.DRY, 0.25f)
                .envFactor(BiomeDictionary.Type.HOT, 0.75f)
                .envFactor(BiomeDictionary.Type.COLD, 1.05f)
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
        level.setBlockAndUpdate(pos.above(), getCapProperties().getDynamicCapState(true));
        // Set to fully fertilized rooty dirt underneath.
        placeRootyDirtBlock(level, pos.below(), 15);

        if (doesRequireTileEntity(level, pos)) {
            SpeciesBlockEntity speciesBlockEntity = DTRegistries.SPECIES_BLOCK_ENTITY.create(pos.below(), level.getBlockState(pos.below()));
            assert speciesBlockEntity != null;
            level.setBlockEntity(speciesBlockEntity);
            speciesBlockEntity.setSpecies(this);
        }

        return true;
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

    @Override
    public boolean isAcceptableSoil(LevelReader level, BlockPos pos, BlockState soilBlockState) {
        return (SoilHelper.isSoilAcceptable(soilBlockState, soilTypeFlags) || level.getRawBrightness(pos.above(), 0) <= getMaxLightForPlanting())
                && super.isAcceptableSoil(level, pos, soilBlockState);
    }

    @Override
    public boolean isAcceptableSoil(BlockState soilState) {
        if (acceptAnySoil){
            Block soilBlock = soilState.getBlock();
            return soilBlock instanceof RootyBlock || SoilHelper.isSoilRegistered(soilBlock);
        }
        return SoilHelper.isSoilAcceptable(soilState, soilTypeFlags);
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        final ResourceLocation capLocation = surround(this.getRegistryName(), "block/", "_cap");
        textureConsumer.accept("particle", capLocation);
        textureConsumer.accept("stem", capLocation);
        textureConsumer.accept("cap", capLocation);
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

    ///////////////////////////////////////////
    // GENERATION
    ///////////////////////////////////////////

    public MushroomInflatorNode getNodeInflator(List<Pair<BlockPos, Integer>> capAges, int radius) {
        return new MushroomInflatorNode(this, capAges, radius);
    }

    @Override
    public JoCode getJoCode(String joCodeString) {
        return new JoCode(joCodeString){
            public void generate(GenerationContext context) {
                LevelAccessor level = context.level();
                if (!(context.species() instanceof HugeMushroomSpecies species)) return;
                int radius = context.radius();
                boolean worldGen = context.safeBounds() != SafeChunkBounds.ANY;

                this.setFacing(context.facing());
                final BlockPos rootPos = species.preGeneration(level, context.rootPos(), radius, context.facing(), context.safeBounds(), this);

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
                final MushroomInflatorNode inflator = species.getNodeInflator(capAges, radius); //This is responsible for gathering a list of branch end points.
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
                if (species.handleRot(level, endPoints, rootPos, treePos, 0, context.safeBounds())) {
                    return; // The entire tree rotted away before it had a chance.
                }

                // Allow for special decorations by the tree itself.
                species.postGeneration(new PostGenerationContext(context, endPoints, initialDirtState));
                MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(level, species, rootPos, endPoints, context.safeBounds(), initialDirtState));

            }

            private void generateMushroomCap(GenerationContext context, HugeMushroomSpecies species, List<Pair<BlockPos, Integer>> capAges){
                LevelAccessor level = context.level();
                final CapProperties capProperties = species.getCapProperties();

                for (Pair<BlockPos, Integer> capAge : capAges){
                    capProperties.getMushroomShapeKit().generateMushroomCap(new MushroomCapContext(level, capAge.getA(), species, capAge.getB()));
                }
            }

            protected void cleanupFrankentree(LevelAccessor level, BlockPos treePos, BlockState treeState, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
                final Set<BlockPos> blocksToDestroy = new HashSet<>();
                final BranchBlock branch = TreeHelper.getBranch(treeState);
                final MapSignal signal = new MapSignal(new CollectorNode(blocksToDestroy));

                signal.destroyLoopedNodes = false;
                signal.trackVisited = true;

                assert branch != null;

                branch.analyse(treeState, level, treePos, null, signal);
                BranchBlock.destroyMode = DynamicTrees.DestroyMode.IGNORE;

                for (BlockPos pos : blocksToDestroy) {
                    if (safeBounds.inBounds(pos, false)) {
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
                                    for (SimpleVoxmap.Cell cell : leafCluster.getAllNonZeroCells()) {
                                        final BlockPos delPos = pos.offset(cell.getPos());
                                        if (safeBounds.inBounds(delPos, false)) {
                                            final BlockState leavesState = level.getBlockState(delPos);
                                            if (TreeHelper.isLeaves(leavesState)) {
                                                final DynamicLeavesBlock leavesBlock = (DynamicLeavesBlock) leavesState.getBlock();
                                                if (leavesProperties.getFamily() == leavesBlock.getProperties(leavesState).getFamily()) {
                                                    level.setBlock(delPos, BlockStates.AIR, 2);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }

                        level.setBlock(pos, BlockStates.AIR, 2);
                    }
                }

                BranchBlock.destroyMode = DynamicTrees.DestroyMode.HARVEST;

                // Now wreck out all surrounding leaves. Let them grow back naturally.
 		/*if (!endPoints.isEmpty()) {
 			final BlockBounds bounds = new BlockBounds(endPoints).expand(3);

 			for (BlockPos pos : bounds) {
 				if (safeBounds.inBounds(pos, false) && TreeHelper.isLeaves(level.getBlockState(pos))) {
					level.setBlock(pos, DTRegistries.BLOCK_STATES.AIR, 2);
				}
 			}
 		}*/
            }
        };
    }

}
