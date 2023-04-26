package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.block.branch.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomFamily;
import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

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
                signal.success = cap.tryGrowCap(level, species.getCapProperties(),0, signal, pos, pos);
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
    public void destroyLeaves(final @NotNull Level level, final @NotNull BlockPos cutPos, final @NotNull Species species, final @NotNull ItemStack tool, final @NotNull List<BlockPos> endPoints, final @NotNull Map<BlockPos, BlockState> destroyedCapBlocks, final @NotNull List<ItemStackPos> drops) {
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
            int age = DynamicCapCenterBlock.getCapAge(level, endPos.above());
            if (age > 0){
                for (final BlockPos findPos : mushSpecies.getCapProperties().getMushroomShapeKit().getShapeCluster(new MushroomCapContext(level, endPos.above(), mushSpecies, age))) {
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
        for (final SimpleVoxmap.Cell cell : capMap.getAllNonZeroCells()) {
            final BlockPos.MutableBlockPos pos = cell.getPos();
            final BlockState state = level.getBlockState(pos);
            if (family.isCompatibleCap(mushSpecies, state, level, pos)) {
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
