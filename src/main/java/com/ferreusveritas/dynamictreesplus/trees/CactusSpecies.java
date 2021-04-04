package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import com.ferreusveritas.dynamictreesplus.init.DTPRegistries;
import com.ferreusveritas.dynamictreesplus.systems.dropcreators.CactusSeedDropCreator;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class CactusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(CactusSpecies::new);

    private final CactusSeedDropCreator cactusSeedDropCreator = new CactusSeedDropCreator();
    private CactusThicknessLogic thicknessLogic;

    public CactusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);

        this.addDropCreator(this.cactusSeedDropCreator);
    }

    @Override
    public Species setPreReloadDefaults() { ;
        this.setTransformable(false);
        return super.setPreReloadDefaults().envFactor(BiomeDictionary.Type.SNOWY, 0.25f).envFactor(BiomeDictionary.Type.COLD, 0.5f)
                .envFactor(BiomeDictionary.Type.SANDY, 1.05f).setGrowthLogicKit(DTPRegistries.PILLAR_LOGIC);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (this.thicknessLogic == null)
            // Try to get the logic kit for the registry name.
            this.thicknessLogic = CactusThicknessLogic.REGISTRY.get(this.getRegistryName());

        return super.setPostReloadDefaults();
    }

    public void setSeedPerBranch(float seedsPerBranch) {
        this.cactusSeedDropCreator.setSeedPerBranch(seedsPerBranch);
    }

    public void setThicknessLogic(CactusThicknessLogic thicknessLogic) {
        this.thicknessLogic = thicknessLogic;
    }

    public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
        return this.thicknessLogic.thicknessAfterGrowthSignal(world, pos, signal, currentThickness);
    }

    public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
        return this.thicknessLogic.thicknessForBranchPlaced(world, pos, isLast);
    }

    @Override
    public boolean showSpeciesOnWaila() {
        return true;
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(DirtHelper.SAND_LIKE);
    }

    @Override
    public JoCode getJoCode(String joCodeString) {
        return new JoCodeCactus(joCodeString);
    }

    @Override
    public boolean isBiomePerfect(Biome biome) {
        return this.perfectBiomes.size() > 0 ? super.isBiomePerfect(biome) : BiomeDictionary.hasType(getBiomeKey(biome), BiomeDictionary.Type.DRY)
                && BiomeDictionary.hasType(getBiomeKey(biome), BiomeDictionary.Type.SANDY);
    }

    @Override
    public boolean handleRot(IWorld world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {
        return false;
    }

    @Override
    public boolean transitionToTree(World world, BlockPos pos) {
        // Ensure planting conditions are right.
        final Family family = getFamily();
        if (world.isEmptyBlock(pos.above()) && this.isAcceptableSoil(world, pos.below(), world.getBlockState(pos.below()))) {
            this.placeRootyDirtBlock(world, pos.below(), 15); // Set to fully fertilized rooty sand underneath.
            world.setBlockAndUpdate(pos, family.getDynamicBranch().defaultBlockState().setValue(CactusBranchBlock.TRUNK_TYPE, this.thicknessForBranchPlaced(world, pos, false)));// Set to a single branch
            return true;
        }

        return false;
    }

    @Override
    public boolean canBoneMealTree() {
        return DTPConfigs.canBoneMealCactus.get();
    }

    @Override
    public VoxelShape getSaplingShape() {
        return VoxelShapes.create(new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.5f, 0.625f));
    }

    public SoundType getSaplingSound() {
        return SoundType.WOOL;
    }

    private static class JoCodeCactus extends JoCode {

        public JoCodeCactus(String code) {
            super(code);
        }

        @Override
        public void generate(World worldObj, IWorld world, Species species, BlockPos rootPos, Biome biome, Direction facing, int radius, SafeChunkBounds safeBounds) {
            BlockState initialDirtState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
            species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt

            // A Tree generation boundary radius is at least 2 and at most 8
            radius = MathHelper.clamp(radius, 2, 8);
            BlockPos treePos = rootPos.above();

            // Create tree
            setFacing(facing);
            generateFork(world, species, 0, rootPos, false);

            // Fix branch thicknesses and map out leaf locations
            BranchBlock branch = TreeHelper.getBranch(world.getBlockState(treePos));
            if(branch != null) {//If a branch exists then the growth was successful
                FindEndsNode endFinder = new FindEndsNode(); // This is responsible for gathering a list of branch end points
                MapSignal signal = new MapSignal(endFinder);
                branch.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN, signal);
                List<BlockPos> endPoints = endFinder.getEnds();

                // Allow for special decorations by the tree itself
                species.postGeneration(worldObj, world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);
                MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds, initialDirtState));
            } else { // The growth failed.. turn the soil back to what it was
                world.setBlock(rootPos, initialDirtState, careful ? 3 : 2);
            }
        }

        @Override
        public boolean setBlockForGeneration(IWorld world, Species species, BlockPos pos, Direction dir, boolean careful, boolean isLast) {
            if (!(species instanceof CactusSpecies))
                return false;
            BlockState defaultBranchState = species.getFamily().getDynamicBranch().defaultBlockState();
            if (world.getBlockState(pos).canBeReplacedByLogs(world, pos) && (!careful || isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
                CactusBranchBlock.CactusThickness trunk = ((CactusSpecies) species).thicknessForBranchPlaced(world, pos, isLast);
                return !world.setBlock(pos, defaultBranchState.setValue(CactusBranchBlock.TRUNK_TYPE, trunk).setValue(CactusBranchBlock.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
            }
            return true;
        }

    }

    public static float getEnergy (World world, BlockPos pos, Species species, float signalEnergy, int mod) {
        long day = world.getGameTime() / 24000L;
        int month = (int)day / 30; //Change the hashs every in-game month

        // Vary the height energy by a psuedorandom hash function
        return signalEnergy * species.biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.above(month), 2) % mod);
    }

}
