package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DynamicCapBlock extends HugeMushroomBlock implements TreePart, UpdatesSurroundNeighbors {

    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 8);

    public CapProperties properties = CapProperties.NULL;

    public DynamicCapBlock(CapProperties capProperties, final Properties properties) {
        this(properties);
        this.setProperties(capProperties);
        capProperties.setDynamicCapState(defaultBlockState(), false);
    }

    public DynamicCapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any().setValue(DISTANCE, 1).setValue(NORTH, true).setValue(EAST, true).setValue(SOUTH, true).setValue(WEST, true).setValue(UP, true).setValue(DOWN, true));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(DISTANCE, UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }

    public void setProperties(CapProperties properties) {
        this.properties = properties;
    }

    public CapProperties getProperties(BlockState blockState) {
        return properties;
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return from.getFamily() == getFamily(state, level, pos) ? 2 : 0;
    }

    //Branches don't connect to the sides of the cap, only the center
    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return 0;
    }

    @Override
    public int getRadius(BlockState state) {
        return 0;
    }

    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        return signal;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos pos) {
        return properties.getFamily();
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return this.getProperties(level.getBlockState(pos)).getFlammability();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return this.getProperties(level.getBlockState(pos)).getFireSpreadSpeed();
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return this.getFlammability(state, level, pos, face) > 0 || face == Direction.UP;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public TreePartType getTreePartType() {
        return TreePartType.OTHER;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getProperties(state).getPrimitiveCapItemStack();
    }

    public static BlockState setDirectionValues (BlockState state, boolean[] directions){
        return state
                .setValue(DOWN,directions[0])
                .setValue(UP,directions[1])
                .setValue(NORTH,directions[2])
                .setValue(SOUTH,directions[3])
                .setValue(WEST,directions[4])
                .setValue(EAST,directions[5]);
    }

    public static boolean[] getDirectionValues (BlockState state){
        if (state.getBlock() instanceof HugeMushroomBlock)
            return new boolean[]{
                    state.getValue(DOWN),
                    state.getValue(UP),
                    state.getValue(NORTH),
                    state.getValue(SOUTH),
                    state.getValue(WEST),
                    state.getValue(EAST)
            };
        return new boolean[6];
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    ///////////////////////////////////////////
    // PARTICLES
    ///////////////////////////////////////////

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand) {
        super.animateTick(pState, pLevel, pPos, pRand);
        if (pRand.nextInt(50) == 0 && pLevel.isEmptyBlock(pPos.below())) {
            pLevel.addParticle(ParticleTypes.WHITE_ASH, (double)pPos.getX() + pRand.nextDouble(), (double)pPos.getY() - 0.1D, (double)pPos.getZ() + pRand.nextDouble(), 1.0D, 0.0D, 1.0D);
        }

    }

    ///////////////////////////////////////////
    // MUSHROOM BLOCK BEHAVIOUR
    ///////////////////////////////////////////

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockGetter blockgetter = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        return this.defaultBlockState()
                .setValue(DOWN, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.below())))
                .setValue(UP, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.above())))
                .setValue(NORTH, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.north())))
                .setValue(EAST, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.east())))
                .setValue(SOUTH, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.south())))
                .setValue(WEST, !properties.isPartOfCap(blockgetter.getBlockState(blockpos.west())));
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return properties.isPartOfCap(pFacingState)
                ? pState.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(pFacing), false)
                : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean destroyed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        //We update neighboring cap blocks in the corners as well
        updateNeighborsSurround(level, pos, DynamicCapBlock.class);
        return destroyed;
    }

    @Override
    public void neighborChanged(BlockState pState, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        level.scheduleTick(pos, pBlock, 0);
        super.neighborChanged(pState, level, pos, pBlock, pFromPos, pIsMoving);
    }

    @Override
    public void tick(BlockState pState, ServerLevel level, BlockPos pos, Random pRandom) {
        if (level.getBlockState(pos).getBlock() != this) return;
        int dist = pState.getValue(DISTANCE);
        boolean supportFound = false;
        for (BlockPos offPos : BlockPos.withinManhattan(pos, 1, 1, 1)){
            if (offPos == pos) continue;
            BlockState offsetState = level.getBlockState(offPos);
            //If we find a cap block the previous age or the center block all is good
            if ((offsetState.hasProperty(DISTANCE) && offsetState.getValue(DISTANCE) == dist - 1)
                    || (dist == 1 && offsetState.getBlock() == properties.getDynamicCapCenterBlock().orElse(null))){
                supportFound = true;
                break;
            }
        }
        if (!supportFound){
            //Otherwise, destroy the block

            level.destroyBlock(pos, true);
            updateNeighborsSurround(level, pos, DynamicCapBlock.class);
            return;
        }
        super.tick(pState, level, pos, pRandom);
    }
}
