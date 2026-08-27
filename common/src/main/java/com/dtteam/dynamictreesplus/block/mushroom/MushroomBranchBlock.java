package com.dtteam.dynamictreesplus.block.mushroom;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.Cell;
import com.dtteam.dynamictrees.api.cell.CellNull;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.systems.nodemapper.DestroyerNode;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.dtteam.dynamictrees.systems.nodemapper.SpeciesNode;
import com.dtteam.dynamictrees.systems.nodemapper.StateNode;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictreesplus.data.DTPLootTableHandler;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.dtteam.dynamictreesplus.tree.HugeMushroomFamily;
import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MushroomBranchBlock extends ThickBranchBlock {

    public MushroomBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
        setFlammability(0); //by default mushrooms don't burn
        setFireSpreadSpeed(0);
    }

    @Override @NotNull
    public Cell getHydrationCell(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Direction dir, @NotNull LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    @Override @NotNull
    public GrowSignal growIntoAir(@NotNull Level level, @NotNull BlockPos pos, GrowSignal signal, int fromRadius) {
        if (!(signal.getSpecies() instanceof final HugeMushroomSpecies species)) return signal;

        final DynamicCapCenterBlock cap = species.getCapCenterBlock().orElse(null);
        if (cap != null) {
            if (fromRadius == getFamily().getPrimaryThickness()) {// If we came from a twig (and we're not a stripped branch) then just make some leaves
                signal.success = cap.tryGrowCap(level, species.getCapProperties(),0, signal, pos, pos, false);
            } else {// Otherwise make a proper branch
                return cap.branchOut(level, pos, signal, 0);
            }
        } else {
            //If the leaves block is null, the branch grows directly without checking for leaves requirements
            if (isNextToBranch(level, pos, signal.dir.getOpposite())) {
                signal.success = false;
                return signal;
            }
            setRadius(level, pos, getFamily().getPrimaryThickness(), null);
            signal.radius = getFamily().getSecondaryThickness();
            signal.success = true;
        }
        return signal;
    }

    public BranchDestructionData destroyBranchFromNode(Level level, BlockPos cutPos, Direction toolDir, boolean wholeTree, @Nullable final LivingEntity entity) {
        final BlockState blockState = level.getBlockState(cutPos);
        final SpeciesNode speciesNode = new SpeciesNode();
        final MapSignal signal = analyse(blockState, level, cutPos, null, new MapSignal(speciesNode)); // Analyze entire tree network to find root node and species.
        final Species species = speciesNode.getSpecies(); // Get the species from the root node.

        // Analyze only part of the tree beyond the break point and map out the extended block states.
        // We can't destroy the branches during this step since we need accurate extended block states that include connections.
        StateNode stateMapper = new StateNode(cutPos);
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(stateMapper));

        // Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches.
        final NetVolumeNode volumeSum = new NetVolumeNode();
        final DestroyerNode destroyer = new DestroyerNode(species).setPlayer(entity instanceof Player ? (Player) entity : null);
        destroyMode = DynamicTrees.DestroyMode.HARVEST;
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;

        // Destroy all the leaves on the branch, store them in a map and convert endpoint coordinates from absolute to relative.
        List<BlockPos> endPoints = destroyer.getEnds();
        final Map<BlockPos, BlockState> destroyedLeaves = new HashMap<>();
        final List<ItemStackPos> leavesDropsList = new ArrayList<>();
        this.destroyMushroomCap(level, cutPos, species, entity == null ? ItemStack.EMPTY : entity.getMainHandItem(), endPoints, destroyedLeaves, leavesDropsList);
        endPoints = endPoints.stream().map(p -> p.subtract(cutPos)).collect(Collectors.toList());

        // Calculate main trunk height.
        int trunkHeight = 1;
        for (BlockPos iter = new BlockPos(0, 1, 0); stateMapper.getBranchConnectionMap().containsKey(iter); iter = iter.above()) {
            trunkHeight++;
        }

        Direction cutDir = signal.localRootDir;
        if (cutDir == null) {
            cutDir = Direction.DOWN;
        }

        BlockState soilState = getCachedSoilState(level, cutPos.offset(cutDir.getUnitVec3i()), true);
        return new BranchDestructionData(species, stateMapper.getBranchConnectionMap(), destroyedLeaves, leavesDropsList, endPoints, volumeSum.getVolume(), cutPos, cutPos, cutDir, toolDir, trunkHeight, soilState);
    }

    //Method is called destroy leaves but this one is to destroy mushroom caps
    public void destroyMushroomCap(final @NotNull Level level, final @NotNull BlockPos cutPos, final @NotNull Species species, final @NotNull ItemStack tool, final @NotNull List<BlockPos> endPoints, final @NotNull Map<BlockPos, BlockState> destroyedCapBlocks, final @NotNull List<ItemStackPos> drops) {
        if (!(species instanceof final HugeMushroomSpecies mushSpecies)) return;
        if (!(species.getFamily() instanceof final HugeMushroomFamily family)) return;

        if (level.isClientSide() || endPoints.isEmpty()) {
            return;
        }

        // Make a bounding volume that holds all the endpoints and expand the volume for the leaves' radius.
        final BlockPosBounds bounds = getFamily().expandLeavesBlockBounds(new BlockPosBounds(endPoints));

        // Create a voxmap to store the leaf destruction map.
        final SimpleVoxmap capMap = new SimpleVoxmap(bounds);

        // For each of the endpoints add an expanded destruction volume around it.
        for (final BlockPos endPos : endPoints) {
            int age = DynamicCapCenterBlock.getCapAge(level, endPos.above());
            if (age >= 0){
                for (final BlockPos findPos : mushSpecies.getMushroomShapeKit().getShapeCluster(new MushroomCapContext(level, endPos.above(), mushSpecies, age))) {
                    final BlockState findState = level.getBlockState(findPos);
                    if (family.isCompatibleCap(mushSpecies, findState, level, findPos)) { // Search for endpoints of the same tree family.
                        capMap.setVoxel(findPos.getX(), findPos.getY(), findPos.getZ(), (byte) 1); // Flag this position for destruction.
                    }
                }
                capMap.setVoxel(endPos, (byte) 0); // We know that the endpoint does not have a leaves block in it because it was a branch.
            }
        }

        final List<ItemStack> dropList = new ArrayList<>();

        // Destroy all family compatible leaves.
        for (final SimpleVoxmap.VoxmapCell cell : capMap.getAllNonZeroCells()) {
            final BlockPos.MutableBlockPos pos = cell.getPos();
            final BlockState state = level.getBlockState(pos);
            if (family.isCompatibleCap(mushSpecies, state, level, pos)) {
                dropList.clear();
                CapProperties cap = getCapProperties(state);
                dropList.addAll(cap.getDrops(level, pos, tool, species));
                final BlockPos imPos = pos.immutable(); // We are storing this so it must be immutable
                final BlockPos relPos = imPos.subtract(cutPos);
                level.setBlock(imPos, Blocks.AIR.defaultBlockState(), 3);
                destroyedCapBlocks.put(relPos, state);
                dropList.forEach(i -> drops.add(new ItemStackPos(i, relPos)));
            }
        }
    }

    private CapProperties getCapProperties (BlockState state){
        if (state.getBlock() instanceof DynamicCapBlock){
            return Optional.of((DynamicCapBlock) state.getBlock())
                .map(block -> block.getProperties(state))
                    .orElse(CapProperties.NULL);
        } else if (state.getBlock() instanceof DynamicCapCenterBlock) {
            return Optional.of((DynamicCapCenterBlock) state.getBlock())
                    .map(block -> block.getProperties(state))
                    .orElse(CapProperties.NULL);
        }
        return CapProperties.NULL;
    }

    @Override
    public LootTable.Builder createBranchDrops(HolderLookup.Provider registries) {
        return DTPLootTableHandler.createMushroomBranchDrops(getPrimitiveLog().get(), registries);
    }

}
