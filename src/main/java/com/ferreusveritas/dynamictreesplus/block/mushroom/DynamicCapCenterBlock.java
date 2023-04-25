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
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

public class DynamicCapCenterBlock extends Block implements TreePart {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 1, 8);

    public CapProperties properties = CapProperties.NULL;

    public DynamicCapCenterBlock(CapProperties capProperties, final Properties properties) {
        this(properties);
        this.setProperties(capProperties);
        capProperties.setDynamicCapState(defaultBlockState(), true);
    }

    public DynamicCapCenterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any().setValue(AGE, 1));
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

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        // Only center of cap should allow for support.
        return radius == branch.getFamily().getPrimaryThickness() && branch.getFamily() == getFamily(state, level, pos) ? BranchBlock.setSupport(0, 1) : 0;
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

    ///////////////////////////////////////////
    // GROWTH
    ///////////////////////////////////////////

    public void updateCap (){

    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        if (signal.step()) // This is always placed at the beginning of every growSignal function.
        {
            BlockState thisState = level.getBlockState(pos);
            int age = thisState.hasProperty(AGE) ? thisState.getValue(AGE) : 0;
            this.branchOut(level, pos, signal, age); // When a growth signal hits a cap block it attempts to become a tree branch.
        } else {
            BlockState thisState = level.getBlockState(pos);
            int age = thisState.hasProperty(AGE) ? thisState.getValue(AGE) : 0;
            if (age != 0 && level.getRandom().nextFloat() < properties.getChanceToAge()){
                age = Math.min(age+1, properties.getMaxAge());
                level.setBlock(pos, getCapBlockStateForPlacement(level, pos, age == 0 ? 1 : age, properties.getDynamicCapState(true), false), 2); // Removed Notify Neighbors Flag for performance.
                generateCap(age, level, signal.getSpecies(), pos);

                Family family = signal.getSpecies().getFamily();
                BlockState capCenter = level.getBlockState(pos);
                int thickness = family.getPrimaryThickness() + (capCenter.hasProperty(DynamicCapCenterBlock.AGE)
                        ? capCenter.getValue(DynamicCapCenterBlock.AGE)
                        : 0);

                BlockPos branchPos = pos.offset(signal.dir.getOpposite().getNormal());
                family.getBranchForPlacement(level, signal.getSpecies(), branchPos).ifPresent(branch ->
                        branch.setRadius(level, branchPos, Math.min(thickness, family.getMaxBranchRadius()), null)
                );
                signal.radius = Math.min(thickness+1, family.getMaxBranchRadius());
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

        boolean couldGrow = tryGrowCap(level, capProperties, age, signal.getSpecies(), pos.relative(signal.dir));

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

    public boolean tryGrowCap(Level level, CapProperties capProp, int currentAge, Species species, BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            int age = currentAge;
            if (currentAge != 0 && level.getRandom().nextFloat() < properties.getChanceToAge())
                age = Math.min(age+1, properties.getMaxAge());
            level.setBlock(pos, getCapBlockStateForPlacement(level, pos, age == 0 ? 1 : age, capProp.getDynamicCapState(true), false), 2); // Removed Notify Neighbors Flag for performance.
            generateCap(age, level, species, pos);
            return true;
        } else {
            final TreePart treePart = TreeHelper.getTreePart(level.getBlockState(pos));
            return treePart instanceof DynamicCapCenterBlock;
        }
    }

    protected void generateCap(int age, Level pLevel, Species species, BlockPos pPos){
        DynamicCapBlock capBlock = properties.getDynamicCapBlock().orElse(null);
        if (capBlock == null) return;

        for (BlockPos pos : BlockPos.withinManhattan(pPos, 8, 8, 8)){
            BlockState posState = pLevel.getBlockState(pos);
            if (posState.is(capBlock)){
                pLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        properties.mushroomShapeKit.generateMushroomCap(new MushroomCapContext(pLevel, pPos, species, age));

    }

    public void placeRing (LevelAccessor level, BlockPos pos, int radius, int step){
        MushroomCapDisc circle = new MushroomCapDisc(pos.getX(), pos.getZ(), radius);

        for (int ix = -circle.radius; ix <= circle.radius; ix++) {
            for (int iz = -circle.radius; iz <= circle.radius; iz++) {
                int circleX = circle.x + ix;
                int circleZ = circle.z + iz;
                if (circle.isEdge(circleX, circleZ)) {
                    level.setBlock(new BlockPos(circleX, pos.getY(), circleZ), getStateForAge(properties, step),2);
                }
            }
        }
    }

    @Nonnull
    private BlockState getStateForAge(CapProperties properties, int age){
        return properties.getDynamicCapState(age);
//        return switch (age) {
//            default -> Blocks.BLACK_CONCRETE.defaultBlockState();
//            case 0 -> Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
//            case 1 -> Blocks.BLUE_CONCRETE.defaultBlockState();
//            case 2 -> Blocks.PURPLE_CONCRETE.defaultBlockState();
//            case 3 -> Blocks.MAGENTA_CONCRETE.defaultBlockState();
//            case 4 -> Blocks.RED_CONCRETE.defaultBlockState();
//            case 5 -> Blocks.ORANGE_CONCRETE.defaultBlockState();
//            case 6 -> Blocks.YELLOW_CONCRETE.defaultBlockState();
//            case 7 -> Blocks.LIME_CONCRETE.defaultBlockState();
//            case 8 -> Blocks.GREEN_CONCRETE.defaultBlockState();
//        };
    }

}
