package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.systems.dropcreators.CactusSeedDropCreator;
import com.ferreusveritas.dynamictreesplus.systems.featuregen.DTPGenFeatures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Optional;

public class Cactus extends TreeFamily {

	public abstract class BaseCactusSpecies extends Species {

		public BaseCactusSpecies (String name, TreeFamily treeFamily){
			super(new ResourceLocation(treeFamily.getName().getNamespace(), name + "_" + treeFamily.getName().getPath()), treeFamily);

			addDropCreator(new CactusSeedDropCreator());

			envFactor(Type.SNOWY, 0.25f);
			envFactor(Type.COLD, 0.5f);
			envFactor(Type.SANDY, 1.05f);
		}

		@Override
		public boolean showSpeciesOnWaila() {
			return true;
		}

		public abstract CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness);

		public abstract CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast);

		@Override
		protected void setStandardSoils() {
			addAcceptableSoils(DirtHelper.SAND_LIKE);
		}

		@Override
		public boolean isTransformable() { return false; }

		@Override
		public JoCode getJoCode(String joCodeString) {
			return new JoCodeCactus(joCodeString);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.DRY) && BiomeDictionary.hasType(biome, Type.SANDY);
		}

		@Override
		public boolean handleRot(IWorld world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {
			return false;
		}

		@Override
		public boolean transitionToTree(World world, BlockPos pos) {
			//Ensure planting conditions are right
			TreeFamily tree = getFamily();
			if(world.isAirBlock(pos.up()) && isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty sand underneath
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState().with(CactusBranchBlock.TRUNK_TYPE, thicknessForBranchPlaced(world, pos, false)));//set to a single branch
				return true;
			}

			return false;
		}

		@Override
		public boolean canBoneMealTree() {
			return true;
		}

		@Override
		public VoxelShape getSaplingShape() {
			return VoxelShapes.create(new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.5f, 0.625f));
		}

		public SoundType getSaplingSound() {
			return SoundType.CLOTH;
		}

	}

	public class SaguaroCactusSpecies extends BaseCactusSpecies {
		
		public SaguaroCactusSpecies(TreeFamily treeFamily) {
			super("saguaro", treeFamily);
			
			setBasicGrowingParameters(tapering, 4.0f, 4, 2, 1.0f);
			
			this.setSoilLongevity(1);

			generateSapling();
			generateSeed();
		}

		@Override
		public boolean getRequiresTileEntity(IWorld world, BlockPos pos) {
			return !isLocationForSaguaro(world, pos);
		}

		@Override
		public Species getMegaSpecies() {
			return megaCactus;
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
			return currentThickness;
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
			BlockState downState = world.getBlockState(pos.down());
			if (TreeHelper.isRooty(downState) || (downState.getBlock() instanceof CactusBranchBlock && downState.get(CactusBranchBlock.TRUNK_TYPE) == CactusBranchBlock.CactusThickness.TRUNK && downState.get(CactusBranchBlock.ORIGIN) == Direction.DOWN))
				return CactusBranchBlock.CactusThickness.TRUNK;
			return CactusBranchBlock.CactusThickness.BRANCH;
		}

		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getGameTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month
			
			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 3);//Vary the height energy by a psuedorandom hash function
		}
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int[] probMap) {
			Direction originDir = signal.dir.getOpposite();
			
			//Alter probability map for direction change
			probMap[0] = 0;//Down is always disallowed for cactus
			probMap[1] = signal.delta.getX() % 2 == 0 || signal.delta.getZ() % 2 == 0 ? getUpProbability() : 0;
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = signal.isInTrunk() && (signal.energy > 1) ? 1 : 0;
			if (signal.dir != Direction.UP) probMap[signal.dir.ordinal()] = 0;//Disable the current direction, unless that direction is up
			probMap[originDir.ordinal()] = 0;//Disable the direction we came from
			return probMap;
		}
		
		@Override
		protected Direction newDirectionSelected(Direction newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != Direction.UP){ //Turned out of trunk
				signal.energy += 0.0f;
			}
			return newDir;
		}
	}

	public class PillarCactusSpecies extends BaseCactusSpecies {

		public PillarCactusSpecies(TreeFamily treeFamily) {
			super("pillar", treeFamily);

			setBasicGrowingParameters(tapering, 8.0f, upProbability, lowestBranchHeight, 1.0f);

			this.setSoilLongevity(1);

			generateSapling();
			generateSeed();
		}

		@Override
		public boolean getRequiresTileEntity(IWorld world, BlockPos pos) {
			return isLocationForSaguaro(world, pos);
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
			BlockState upState = world.getBlockState(pos.up());
			BlockState downState = world.getBlockState(pos.down());
			return (upState.getBlock() instanceof CactusBranchBlock && downState.getBlock() instanceof CactusBranchBlock) ? CactusBranchBlock.CactusThickness.TRUNK : CactusBranchBlock.CactusThickness.BRANCH;
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
			BlockState downState = world.getBlockState(pos.down());
			if (TreeHelper.isRooty(downState) || isLast)
				return CactusBranchBlock.CactusThickness.BRANCH;
			return CactusBranchBlock.CactusThickness.TRUNK;
		}

		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getGameTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month

			return super.getEnergy(world, pos) * biomeSuitability(world, pos) - (CoordUtils.coordHashCode(pos.up(month), 2) % 5);//Vary the height energy by a psuedorandom hash function
		}

		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int[] probMap) {
			//All directions except up are disabled, since pillar cacti are just straight
			return new int[]{0,1,0,0,0,0};
		}
	}

	public class PipeCactusSpecies extends BaseCactusSpecies {

		public PipeCactusSpecies(TreeFamily treeFamily) {
			super("pipe", treeFamily);

			setBasicGrowingParameters(tapering, 4.0f, upProbability, lowestBranchHeight, 1.0f);

			this.setSoilLongevity(1);

			generateSapling();
			generateSeed();

			setRequiresTileEntity(true);

			addGenFeature(DTPGenFeatures.CACTULINGS);
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
			return CactusBranchBlock.CactusThickness.BRANCH;
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
			return CactusBranchBlock.CactusThickness.BRANCH;
		}

		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getGameTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month

			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 3);//Vary the height energy by a psuedorandom hash function
		}

		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int[] probMap) {
			//All directions except up are disabled, since pipe cacti are just straight
			return new int[]{0,1,0,0,0,0};
		}
	}

	public class MegaCactusSpecies extends BaseCactusSpecies {

		private static final int stopBranchingHeight = 5;
		private static final int maxHeight = 7;

		public MegaCactusSpecies(TreeFamily treeFamily) {
			super("mega", treeFamily);

			setBasicGrowingParameters(tapering, 18.0f, 1, 3, 0.6f);

			this.setSoilLongevity(8);

			setRequiresTileEntity(true);
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
			Block down = world.getBlockState(pos.down()).getBlock();
			Block down2 = world.getBlockState(pos.down(2)).getBlock();
			Block down3 = world.getBlockState(pos.down(3)).getBlock();
			if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
				return CactusBranchBlock.CactusThickness.CORE;
			return CactusBranchBlock.CactusThickness.TRUNK;
		}

		@Override
		public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
			Block down = world.getBlockState(pos.down()).getBlock();
			Block down2 = world.getBlockState(pos.down(2)).getBlock();
			Block down3 = world.getBlockState(pos.down(3)).getBlock();

			if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
				return CactusBranchBlock.CactusThickness.CORE;
			if (down instanceof CactusBranchBlock)
				return CactusBranchBlock.CactusThickness.TRUNK;
			return CactusBranchBlock.CactusThickness.BRANCH;
		}

		@Override
		public ItemStack getSeedStack(int qty) {
			return saguaroCactus.getSeedStack(qty);
		}

		@Override
		public Optional<Seed> getSeed() {
			return saguaroCactus.getSeed();
		}

		@Override
		public boolean isMega() {
			return true;
		}

		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getGameTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month

			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 6);//Vary the height energy by a psuedorandom hash function
		}

		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int[] probMap) {
			Direction originDir = signal.dir.getOpposite();

			int height = pos.getY() - signal.rootPos.getY();

			if (height >= maxHeight && world.rand.nextFloat() < 0.8f){
				signal.energy = 0;
				return new int[]{0,0,0,0,0,0};
			}
			if (height > stopBranchingHeight){
				//When above a certain height, all branches should grow straight up
				return new int[]{0,1,0,0,0,0};
			}
			//Alter probability map for direction change
			probMap[0] = 0; //Down is always disallowed for cactus
			probMap[1] = (int)(getUpProbability() + signal.rootPos.distanceSq(pos.getX(), signal.rootPos.getY(), pos.getZ(), true) * 0.8);
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = world.getBlockState(pos.up()).getBlock() instanceof CactusBranchBlock && signal.energy > 1 ? 3 : 0;
			if (signal.dir != Direction.UP) probMap[signal.dir.ordinal()] = 0; //Disable the current direction, unless that direction is up
			probMap[originDir.ordinal()] = 0; //Disable the direction we came from

			return probMap;
		}
	}

	protected boolean isLocationForSaguaro(IWorld world, BlockPos trunkPos){
		return Species.isOneOfBiomes(Species.getBiomeKey(world.getBiome(trunkPos)), Biomes.BADLANDS, Biomes.BADLANDS_PLATEAU);
	}

	Species saguaroCactus;
	Species pipeCactus;
	Species megaCactus;
	public Cactus() {
		super(new ResourceLocation(DynamicTreesPlus.MOD_ID, "cactus"));
		
		setPrimitiveLog(Blocks.CACTUS);
		setStick(Items.AIR);

		addSpeciesLocationOverride((world, trunkPos) -> isLocationForSaguaro(world, trunkPos) ? saguaroCactus : Species.NULL_SPECIES);
	}
	
	@Override
	public ILeavesProperties getCommonLeaves() {
		return new LeavesProperties(null, TreeRegistry.findCellKit("bare"));//Explicitly unbuilt since there's no leaves
	}
	
	@Override
	public BranchBlock createBranch() {
		String branchName = this.getName() + "_branch";
		return new CactusBranchBlock( branchName);
	}

	@Override
	public boolean hasStrippedBranch() {
		return false;
	}

	@Override
	public float getPrimaryThickness() {
		return 5.0f;
	}
	
	@Override
	public float getSecondaryThickness() {
		return 4.0f;
	}
	
	@Override
	public void createSpecies() {
		saguaroCactus = new SaguaroCactusSpecies(this);
		pipeCactus = new PipeCactusSpecies(this);
		megaCactus = new MegaCactusSpecies(this);
		setCommonSpecies(new PillarCactusSpecies(this));
	}

	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.registerAll(saguaroCactus, pipeCactus, megaCactus);
	}

	@Override
	public List<Item> getRegisterableItems(List<Item> itemList) {
		saguaroCactus.getSeed().ifPresent(itemList::add);
		pipeCactus.getSeed().ifPresent(itemList::add);
		return super.getRegisterableItems(itemList);
	}

	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		saguaroCactus.getSapling().ifPresent(blockList::add);
		pipeCactus.getSapling().ifPresent(blockList::add);
		return super.getRegisterableBlocks(blockList);
	}

	protected class JoCodeCactus extends JoCode {
		
		public JoCodeCactus(String code) {
			super(code);
		}
		
		@Override
		public void generate(World worldObj, IWorld world, Species species, BlockPos rootPos, Biome biome, Direction facing, int radius, SafeChunkBounds safeBounds) {
			BlockState initialDirtState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
			species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt
			
			// A Tree generation boundary radius is at least 2 and at most 8
			radius = MathHelper.clamp(radius, 2, 8);
			BlockPos treePos = rootPos.up();
			
			// Create tree
			setFacing(facing);
			generateFork(world, species, 0, rootPos, false);
			
			// Fix branch thicknesses and map out leaf locations
			BranchBlock branch = TreeHelper.getBranch(world.getBlockState(treePos));
			if(branch != null) {//If a branch exists then the growth was successful
				NodeFindEnds endFinder = new NodeFindEnds(); // This is responsible for gathering a list of branch end points
				MapSignal signal = new MapSignal(endFinder);
				branch.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN, signal);
				List<BlockPos> endPoints = endFinder.getEnds();
				
				// Allow for special decorations by the tree itself
				species.postGeneration(worldObj, world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);
				MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds, initialDirtState));
			} else { // The growth failed.. turn the soil back to what it was
				world.setBlockState(rootPos, initialDirtState, careful ? 3 : 2);
			}
		}
		
		@Override
		public boolean setBlockForGeneration(IWorld world, Species species, BlockPos pos, Direction dir, boolean careful, boolean isLast) {
			if (!(species instanceof BaseCactusSpecies))
				return false;
			BlockState defaultBranchState = species.getFamily().getDynamicBranch().getDefaultState();
			if (world.getBlockState(pos).canBeReplacedByLogs(world, pos) && (!careful || isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
				CactusBranchBlock.CactusThickness trunk = ((BaseCactusSpecies) species).thicknessForBranchPlaced(world, pos, isLast);
				return !world.setBlockState(pos, defaultBranchState.with(CactusBranchBlock.TRUNK_TYPE, trunk).with(CactusBranchBlock.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
			}
			return true;
		}
		
	}
	
}
