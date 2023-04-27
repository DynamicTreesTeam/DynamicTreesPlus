package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.systems.poissondisc.Vec2i;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomCapDisc;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DynamicCapCenterBlock extends Block implements TreePart, UpdatesSurroundNeighbors {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 8);

    public CapProperties properties = CapProperties.NULL;

    public DynamicCapCenterBlock(CapProperties capProperties, final Properties properties) {
        this(properties);
        this.setProperties(capProperties);
        capProperties.setDynamicCapState(defaultBlockState(), true);
    }

    public DynamicCapCenterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE);
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

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return properties.getRadiusForConnection(state, level, pos, from, side, fromRadius);
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

    // Only center of cap should allow for support.
    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return branch.getFamily() == getFamily(state, level, pos) ? BranchBlock.setSupport(0, 2) : 0;
    }

    @Override
    public TreePartType getTreePartType() {
        return TreePartType.OTHER;
    }

    public BlockState getCapBlockStateForPlacement(LevelAccessor level, BlockPos pos, int age, BlockState cap, boolean worldGen) {
        if (cap.hasProperty(AGE))
            return cap.setValue(AGE, age); //by default just pass the blockstate along
        return cap;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getProperties(state).getPrimitiveCapItemStack();
    }

    public static int getCapAge (Level level, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DynamicCapCenterBlock) return state.getValue(AGE);
        return -1;
    }

    ///////////////////////////////////////////
    // GROWTH
    ///////////////////////////////////////////

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        if (signal.step()) // This is always placed at the beginning of every growSignal function.
        {
            BlockState thisState = level.getBlockState(pos);
            int age = thisState.hasProperty(AGE) ? thisState.getValue(AGE) : 0;
            if (age != 0 && level.getRandom().nextFloat() < properties.getChanceToAge()){
                tryGrowCap(level, properties, age, signal, pos, pos);
            } else {
                this.branchOut(level, pos, signal, age); // When a growth signal hits a cap block it attempts to become a tree branch.
            }
        }
        return signal;
    }

    public GrowSignal branchOut(Level level, BlockPos pos, GrowSignal signal, int age) {
        if (!(signal.getSpecies() instanceof HugeMushroomSpecies species)) return signal;
        CapProperties capProperties = species.getCapProperties();

        if (BranchBlock.isNextToBranch(level, pos, signal.dir.getOpposite())) {
            signal.success = false;
            return signal;
        }

        boolean couldGrow = tryGrowCap(level, capProperties, age, signal, pos.relative(signal.dir), pos);

        if (couldGrow) {
            Family family = species.getFamily();

            BlockState capCenter = level.getBlockState(pos.offset(signal.dir.getNormal()));
            int thickness = family.getPrimaryThickness() + (capCenter.hasProperty(DynamicCapCenterBlock.AGE)
                    ? capCenter.getValue(DynamicCapCenterBlock.AGE)
                    : 0);

            family.getBranchForPlacement(level, species, pos).ifPresent(branch ->
                    branch.setRadius(level, pos, Math.min(thickness, family.getMaxBranchRadius()), null)
            );
            signal.radius = Math.min(thickness+1, family.getMaxBranchRadius());//For the benefit of the parent branch
        }

        signal.success = couldGrow;

        return signal;
    }

    public boolean tryGrowCap(Level level, CapProperties capProp, int currentAge, GrowSignal signal, BlockPos pos, BlockPos previousPos) {
        if (level.isEmptyBlock(pos)) {
            int age = currentAge;
            if (currentAge == 0){
                age = 1;
            } else if (level.getRandom().nextFloat() < properties.getChanceToAge())
                age = Math.min(age+1, properties.getMaxAge());
            level.setBlock(pos, getCapBlockStateForPlacement(level, pos, age == 0 ? 1 : age, capProp.getDynamicCapState(true), false), 2); // Removed Notify Neighbors Flag for performance.
            if (age != currentAge){
                ageBranchUnderCap(level, pos, signal, currentAge);
            }
            if (!(signal.getSpecies() instanceof HugeMushroomSpecies)) return false;
            generateCap(age, level, (HugeMushroomSpecies) signal.getSpecies(), pos, previousPos, currentAge, signal.rootPos);
            return true;
        } else {
            final TreePart treePart = TreeHelper.getTreePart(level.getBlockState(pos));
            return treePart instanceof DynamicCapCenterBlock;
        }
    }

    protected void ageBranchUnderCap (Level level, BlockPos pos, GrowSignal signal, int currentAge){
        Family family = signal.getSpecies().getFamily();
        int thickness = family.getPrimaryThickness() + currentAge;

        BlockPos branchPos = pos.offset(signal.dir.getOpposite().getNormal());
        family.getBranchForPlacement(level, signal.getSpecies(), branchPos).ifPresent(branch ->
                branch.setRadius(level, branchPos, Math.min(thickness, family.getMaxBranchRadius()), null)
        );
        signal.radius = Math.min(thickness+1, family.getMaxBranchRadius());
    }

    protected void generateCap(int newAge, Level pLevel, HugeMushroomSpecies species, BlockPos newPos, BlockPos currentPos, int currentAge, BlockPos rootPos){
        DynamicCapBlock capBlock = properties.getDynamicCapBlock().orElse(null);
        if (capBlock == null) return;
        //only clear the cap if the position changed or if the age changed
        if (currentPos != newPos || currentAge != newAge)
            properties.mushroomShapeKit.clearMushroomCap(new MushroomCapContext(pLevel, currentPos, species, currentAge));
        properties.mushroomShapeKit.generateMushroomCap(new MushroomCapContext(pLevel, newPos, species, newAge));

    }

    public void clearRing (LevelAccessor level, BlockPos pos, int radius){
        List<Vec2i> ring = MushroomCapDisc.getPrecomputedRing(radius);

        for (Vec2i vec : ring){
            BlockPos ringPos = new BlockPos(pos.getX() + vec.x, pos.getY(), pos.getZ() + vec.z);
            if (properties.isPartOfCap(level.getBlockState(ringPos)))
                level.setBlock(ringPos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    public void placeRing (LevelAccessor level, BlockPos pos, int radius, int step, boolean yMoved, boolean negFactor){
        List<Vec2i> ring = MushroomCapDisc.getPrecomputedRing(radius);

        for (Vec2i vec : ring){
            BlockPos ringPos = new BlockPos(pos.getX() + vec.x, pos.getY(), pos.getZ() + vec.z);
            if (level.getBlockState(ringPos).getMaterial().isReplaceable())
                level.setBlock(ringPos, getStateForAge(properties, step, new Vec2i(-vec.x,-vec.z), yMoved, negFactor, properties.isPartOfCap(level.getBlockState(ringPos.above()))),2);
        }
    }

    public List<BlockPos> getRing (LevelAccessor level, BlockPos pos, int radius){
        List<Vec2i> ring = MushroomCapDisc.getPrecomputedRing(radius);
        List<BlockPos> positions = new LinkedList<>();
        for (Vec2i vec : ring){
            BlockPos ringPos = new BlockPos(pos.getX() + vec.x, pos.getY(), pos.getZ() + vec.z);
            if (properties.isPartOfCap(level.getBlockState(ringPos)))
                positions.add(ringPos);
        }
        return positions;
    }

    @Nonnull
    private BlockState getStateForAge(CapProperties properties, int age, Vec2i centerDirection, boolean yMoved, boolean negativeFactor, boolean topIsCap){
        boolean[] dirs = {false, !topIsCap, true, true, true, true};
        if (yMoved || age == 1){
            for (Direction dir : Direction.Plane.HORIZONTAL){
                float dot = dir.getNormal().getX() * centerDirection.x + dir.getNormal().getZ() * centerDirection.z;
                if (dot > 0)
                    dirs[negativeFactor ? dir.getOpposite().ordinal() : dir.ordinal()] = false;
            }
        }
        return properties.getDynamicCapState(age, dirs);

    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean destroyed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        //We update neighboring cap blocks in the corners as well
        updateNeighborsSurround(level, pos, DynamicCapBlock.class);
        return destroyed;
    }

    ///////////////////////////////////////////
    // SHAPE
    ///////////////////////////////////////////

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.is(this) && pState.getValue(AGE) == 0)
            return properties.getAgeZeroShape();
        return super.getShape(pState, pLevel, pPos, pContext);
    }

}
