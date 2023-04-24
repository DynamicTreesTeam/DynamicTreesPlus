package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomFamily;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MushroomBranchBlock extends ThickBranchBlock {

    public MushroomBranchBlock(ResourceLocation name, Properties properties) {
        super(name, properties);
        setFlammability(0); //by default mushrooms don't burn
        setFireSpreadSpeed(0);
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    public GrowSignal growIntoAir(Level level, BlockPos pos, GrowSignal signal, int fromRadius) {
        if (!(signal.getSpecies() instanceof final HugeMushroomSpecies species)) return signal;

        final DynamicCapCenterBlock cap = species.getCapCenterBlock().orElse(null);
        if (cap != null) {
            if (fromRadius == getFamily().getPrimaryThickness()) {// If we came from a twig (and we're not a stripped branch) then just make some leaves
                signal.success = cap.tryGrowCap(level, species.getCapProperties(), 0, pos);
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

    //Method is called destroy leaves but this one is to destroy mushroom caps
    public void destroyLeaves(final Level level, final BlockPos cutPos, final Species species, final ItemStack tool, final List<BlockPos> endPoints, final Map<BlockPos, BlockState> destroyedCapBlocks, final List<ItemStackPos> drops) {
        if (!(species instanceof final HugeMushroomSpecies mushSpecies)) return;
        if (!(species.getFamily() instanceof final HugeMushroomFamily family)) return;

        if (level.isClientSide || endPoints.isEmpty()) {
            return;
        }

        // Make a bounding volume that holds all of the endpoints and expand the volume for the leaves radius.
        final BlockBounds bounds = getFamily().expandLeavesBlockBounds(new BlockBounds(endPoints));

        // Create a voxmap to store the leaf destruction map.
        final SimpleVoxmap capMap = new SimpleVoxmap(bounds);

        // For each of the endpoints add an expanded destruction volume around it.
        for (final BlockPos endPos : endPoints) {
            for (final BlockPos leafPos : getFamily().expandLeavesBlockBounds(new BlockBounds(endPos))) {
                capMap.setVoxel(leafPos, (byte) 1); // Flag this position for destruction.
            }
            capMap.setVoxel(endPos, (byte) 0); // We know that the endpoint does not have a leaves block in it because it was a branch.
        }

        final BranchBlock familyBranch = family.getBranch().get();
        final int primaryThickness = family.getPrimaryThickness();

        // Expand the volume yet again in all directions and search for other non-destroyed endpoints.
        for (final BlockPos findPos : getFamily().expandLeavesBlockBounds(bounds)) {
            final BlockState findState = level.getBlockState(findPos);
            if (familyBranch.getRadius(findState) == primaryThickness) { // Search for endpoints of the same tree family.
                final Iterable<BlockPos.MutableBlockPos> blocks = mushSpecies.getCapProperties().getMushroomShapeKit().getShapeCluster().getAllNonZero();
                for (BlockPos.MutableBlockPos capPos : blocks) {
                    capMap.setVoxel(findPos.getX() + capPos.getX(), findPos.getY() + capPos.getY(), findPos.getZ() + capPos.getZ(), (byte) 0);
                }
            }
        }

        final List<ItemStack> dropList = new ArrayList<>();

        // Destroy all family compatible leaves.
        for (final SimpleVoxmap.Cell cell : capMap.getAllNonZeroCells()) {
            final BlockPos.MutableBlockPos pos = cell.getPos();
            final BlockState state = level.getBlockState(pos);
            if (family.isCompatibleCap(species, state, level, pos)) {
                dropList.clear();
                CapProperties cap = getCapProperties(state);
                dropList.addAll(cap.getDrops(level, pos, tool, species));
                final BlockPos imPos = pos.immutable(); // We are storing this so it must be immutable
                final BlockPos relPos = imPos.subtract(cutPos);
                level.setBlock(imPos, BlockStates.AIR, 3);
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

}
