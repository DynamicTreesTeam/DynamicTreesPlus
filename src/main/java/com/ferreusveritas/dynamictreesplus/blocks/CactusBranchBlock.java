package com.ferreusveritas.dynamictreesplus.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.Cell;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.entry.SeedItemLootEntry;
import com.ferreusveritas.dynamictrees.loot.function.MultiplyLogsCount;
import com.ferreusveritas.dynamictrees.loot.function.MultiplySticksCount;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Connections;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class CactusBranchBlock extends BranchBlock {

	// The direction it grew from. Can't be up, since cacti can't grow down.
	public static final EnumProperty<Direction> ORIGIN = EnumProperty.create("origin", Direction.class, (Predicate<Direction>) dir -> dir != Direction.UP);
	 // Not sure it's technically called the 'trunk' on cacti, but whatever
	public static final EnumProperty<CactusThickness> TRUNK_TYPE = EnumProperty.create("type", CactusThickness.class);

	public enum CactusThickness implements IStringSerializable {
		BRANCH("branch", 4),
		TRUNK("trunk", 5),
		CORE("core", 7);
		final String name;
		final int radius;
		CactusThickness (String name, int radius) { this.name = name; this.radius = radius; }
		public int getRadius() { return radius; }
		@Override public String toString() { return this.name; }
		@Override public String getSerializedName() { return this.name; }
	}

	public CactusBranchBlock(ResourceLocation name, Properties properties) {
		super(name, properties);

		this.registerDefaultState(this.getStateDefinition().any().setValue(TRUNK_TYPE, CactusThickness.TRUNK).setValue(ORIGIN, Direction.DOWN));
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ORIGIN, TRUNK_TYPE);
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
		return 0; // Cacti don't have leaves and don't rot.
	}

	///////////////////////////////////////////
	// PHYSICAL PROPERTIES
	///////////////////////////////////////////

	@Override
	public float getHardness(BlockState state, IBlockReader world, BlockPos pos) {
		final int radius = this.getRadius(state);
		final float hardness = this.getFamily().getPrimitiveLog().orElse(Blocks.AIR).defaultBlockState()
				.getDestroySpeed(world, pos) * (radius * radius) / 64.0f * 8.0f;
		return (float) Math.min(hardness, DTConfigs.MAX_TREE_HARDNESS.get()); // So many youtube let's plays start with "OMG, this is taking so long to break this tree!"
	}

	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////

	@Override
	public boolean checkForRot(IWorld world, BlockPos pos, Species species, int radius, int fertility, Random rand, float chance, boolean rapid) {
		return false;//Do nothing.  Cacti don't rot
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	private static final double hurtMovementDelta = 0.003;

	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entity) {
		boolean damage = false;
		if (DTPConfigs.cactusPrickleOnMoveOnly.get() && entity instanceof LivingEntity) {
			boolean falling = entity.getDeltaMovement().y < 0;
			entity.setDeltaMovement(entity.getDeltaMovement().x * 0.25, entity.getDeltaMovement().y * (falling?0.5:1), entity.getDeltaMovement().z  * 0.25);
			if (!worldIn.isClientSide && (entity.xOld != entity.getX() || entity.yOld != entity.getY() || entity.zOld != entity.getZ())) {
				double xMovement = Math.abs(entity.getX() - entity.xOld);
				double yMovement = Math.abs(entity.getY() - entity.yOld);
				double zMovement = Math.abs(entity.getZ() - entity.zOld);
				if (xMovement >= hurtMovementDelta || yMovement >= hurtMovementDelta || zMovement >= hurtMovementDelta) {
					damage = true;
				}
			}
		} else if (!(entity instanceof ItemEntity) || DTPConfigs.cactusKillItems.get()) {
			damage = true;
		}

		if (damage) entity.hurt(DamageSource.CACTUS, 1.0F);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState returnState = this.defaultBlockState();

		BlockState adjState = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
		boolean trunk = (context.getClickedFace() == Direction.UP && (adjState.getBlock() != this || adjState.getValue(TRUNK_TYPE) != CactusThickness.BRANCH));

		return returnState.setValue(TRUNK_TYPE, trunk?CactusThickness.TRUNK:CactusThickness.BRANCH).setValue(ORIGIN, context.getClickedFace() != Direction.DOWN ? context.getClickedFace().getOpposite() : Direction.DOWN);
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public Cell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, LeavesProperties leavesProperties) {
		return CellNull.NULL_CELL;
	}

	protected int getCactusRadius(CactusThickness trunk){
		switch (trunk){
			default:
			case BRANCH:
				return (int)getFamily().getSecondaryThickness();
			case TRUNK:
				return (int)getFamily().getPrimaryThickness();
			case CORE:
				return 7;
		}
	}

	@Override
	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? getCactusRadius(blockState.getValue(TRUNK_TYPE)) : 0;
	}

	@Override
	public int setRadius(IWorld world, BlockPos pos, int radius, Direction originDir, int flags) {
		destroyMode = DynamicTrees.DestroyMode.SET_RADIUS;
		world.setBlock(pos, getStateForRadius(radius).setValue(ORIGIN, originDir), flags);
		destroyMode = DynamicTrees.DestroyMode.SLOPPY;
		return radius;
	}

	// Directionless probability grabber
	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from) {
		return isSameTree(from) ? getRadius(blockState) + 2 : 0;
	}

	public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal) {
		Direction originDir = signal.dir.getOpposite(); // Direction this signal originated from

		CactusThickness trunk;
		if (signal.getSpecies() instanceof CactusSpecies){
			trunk = ((CactusSpecies) signal.getSpecies()).thicknessForBranchPlaced(world, pos, true);
		} else trunk = CactusThickness.BRANCH;

		if (originDir.getAxis() != Direction.Axis.Y && (world.getBlockState(pos.above()).getBlock() == this || world.getBlockState(pos.below()).getBlock() == this)) {
			signal.success = false;
			return signal;
		}

		signal.success = world.setBlock(pos, this.stateDefinition.any().setValue(TRUNK_TYPE, trunk).setValue(ORIGIN, originDir), 2);
		signal.radius = getCactusRadius(trunk);
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {

		if (signal.step()) { // This is always placed at the beginning of every growSignal function
			Species species = signal.getSpecies();

			//Direction originDir = signal.dir.getOpposite(); // Direction this signal originated from
			Direction targetDir = species.getGrowthLogicKit().selectNewDirection(
					new DirectionSelectionContext(world, pos, signal.getSpecies(), this, signal)
			); // This must be cached on the stack for proper recursion
			signal.doTurn(targetDir);

			BlockPos deltaPos = pos.relative(targetDir);
			BlockState deltaState = world.getBlockState(deltaPos);

			// Pass grow signal to next block in path
			TreePart treepart = TreeHelper.getTreePart(deltaState);

			if (treepart == this) {
				signal = treepart.growSignal(world, deltaPos, signal); // Recurse
			} else if (world.isEmptyBlock(deltaPos)) {
				signal = growIntoAir(world, deltaPos, signal);
			}

			BlockState thisState = world.getBlockState(pos);
			if (thisState.getBlock() == this && species instanceof CactusSpecies){
				CactusThickness isTrunk = thisState.getValue(TRUNK_TYPE);
				CactusThickness newIsTrunk = ((CactusSpecies) species).thicknessAfterGrowthSignal(world, pos, signal, isTrunk);
				if (isTrunk != newIsTrunk){
					setRadius(world, pos, getCactusRadius(newIsTrunk), thisState.getValue(ORIGIN));
				}
			}

		}

		return signal;
	}

	@Override
	public BlockState getStateForRadius(int radius) {
		CactusThickness thickness = CactusThickness.BRANCH;
		if (radius >= getCactusRadius(CactusThickness.CORE)) thickness = CactusThickness.CORE;
		else if (radius >= getCactusRadius(CactusThickness.TRUNK)) thickness = CactusThickness.TRUNK;
		return defaultBlockState().setValue(TRUNK_TYPE, thickness);
	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////


	@Override
	public Connections getConnectionData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		Connections connections = new Connections();

		for (Direction dir : Direction.values()) {
			connections.setRadius(dir, this.getSideConnectionRadius(world, pos, dir));
		}

		return connections;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		int thisRadius = getRadius(state);

		VoxelShape shape = VoxelShapes.empty();

		int numConnections = 0;
		for (Direction dir : Direction.values()) {
			int connRadius = getSideConnectionRadius(worldIn, pos, dir);
			if (connRadius > 0) {
				numConnections++;
				double radius = MathHelper.clamp(connRadius, 1, thisRadius) / 16.0;
				double gap = 0.5 - radius;
				AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).inflate(radius);
				aabb = aabb.move(dir.getStepX() * gap, dir.getStepY() * gap, dir.getStepZ() * gap).move(0.5, 0.5, 0.5);
				shape = VoxelShapes.joinUnoptimized(shape, VoxelShapes.create(aabb), IBooleanFunction.OR);
			}
		}
		if (state.getValue(TRUNK_TYPE) == CactusThickness.BRANCH && numConnections == 1 && state.getValue(ORIGIN).getAxis().isHorizontal()) {
			double radius = MathHelper.clamp(getCactusRadius(CactusThickness.BRANCH), 1, thisRadius) / 16.0;
			double gap = 0.5 - radius;
			AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).inflate(radius);
			aabb = aabb.move(Direction.UP.getStepX() * gap, Direction.UP.getStepY() * gap, Direction.UP.getStepZ() * gap).move(0.5, 0.5, 0.5);
			shape = VoxelShapes.joinUnoptimized(shape, VoxelShapes.create(aabb), IBooleanFunction.OR);
		}

		double min = 0.5 - (thisRadius / 16.0), max = 0.5 + (thisRadius / 16.0);
		shape = VoxelShapes.joinUnoptimized(shape, VoxelShapes.create(new AxisAlignedBB(min, min, min, max, max, max)), IBooleanFunction.OR);
		return shape;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return this.getRadius(blockState);
	}

	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, Direction side) {
		BlockPos deltaPos = pos.relative(side);

		final BlockState otherState = CoordUtils.getStateSafe(blockAccess, deltaPos);
		final BlockState state = CoordUtils.getStateSafe(blockAccess, pos);

		// If the blocks aren't loaded, assume there is no connection.
		if (otherState == null || state == null || state.getBlock() != this)
			return 0;

		if (otherState.getBlock() == this && (otherState.getValue(ORIGIN) == side.getOpposite() || state.getValue(ORIGIN) == side)) {
			return Math.min(getCactusRadius(state.getValue(TRUNK_TYPE)), getCactusRadius(otherState.getValue(TRUNK_TYPE)));
		} else if (side == Direction.DOWN && state.getValue(ORIGIN) == side && (otherState.getBlock() == this || otherState.getBlock() instanceof RootyBlock)) {
			return getCactusRadius(state.getValue(TRUNK_TYPE));
		}

		return 0;
	}

	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////

	@Override
	public MapSignal analyse(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node
		if (signal.depth++ < 32) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			BlockState state = world.getBlockState(pos);
			signal.run(blockState, world, pos, fromDir);// Run the inspectors of choice
			for (Direction dir : Direction.values()) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.relative(dir);
					BlockState deltaState = world.getBlockState(deltaPos);

					if (deltaState.getBlock() == this && deltaState.getValue(ORIGIN) == dir.getOpposite()) {
						signal = ((TreePart) deltaState.getBlock()).analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);
					} else if (state.getBlock() == this && state.getValue(ORIGIN) == dir) {
						signal = TreeHelper.getTreePart(deltaState).analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);
					}

					// This should only be true for the originating block when the root node is found
					if (signal.foundRoot && signal.localRootDir == null && fromDir == null) {
						signal.localRootDir = dir;
					}
				}
			}
			signal.returnRun(blockState, world, pos, fromDir);
		} else {
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof BranchBlock) {
				BranchBlock branch = (BranchBlock) state.getBlock();
				branch.breakDeliberate(world, pos, DynamicTrees.DestroyMode.OVERFLOW);// Destroy one of the offending nodes
			}
			signal.overflow = true;
		}
		signal.depth--;
		return signal;
	}

	@Override
	public LootTable.Builder createBranchDrops() {
		return LootTable.lootTable().withPool(
				LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
						ItemLootEntry.lootTableItem(getPrimitiveLog().get())
								.apply(MultiplyLogsCount.multiplyLogsCount())
								.apply(ExplosionDecay.explosionDecay())
				)
		).withPool(
				LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
						SeedItemLootEntry.lootTableSeedItem()
								.apply(MultiplySticksCount.multiplySticksCount())
								.apply(ExplosionDecay.explosionDecay())
				)
		).setParamSet(DTLootParameterSets.BRANCHES);
	}
}
