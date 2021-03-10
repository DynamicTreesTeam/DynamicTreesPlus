package com.ferreusveritas.dynamictreesplus.systems.dropcreators;

import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * A drop creator that drops apples just like Vanilla trees.
 * No longer used by any of the trees in the base mod on account of
 * the addition of the apple species.  Left for demonstration purposes
 * or in case I change my mind about something.
 * 
 * @author ferreusveritas
 *
 */
public class CactusSeedDropCreator extends DropCreator {

    private float seedPerBranch = 0.5f;

    public CactusSeedDropCreator() {
        super(new ResourceLocation(DynamicTreesPlus.MOD_ID, "cactusseeds"));
    }

    public CactusSeedDropCreator setSeedPerBranch(float seedPerLog) {
        this.seedPerBranch = seedPerLog;
        return this;
    }

    @Override
    public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NetVolumeNode.Volume volume) {
        int numLogs = (int) (volume.getVolume() * this.seedPerBranch);
        while (numLogs > 0) {
            dropList.add(species.getSeedStack(Math.min(numLogs, 64)));
            numLogs -= 64;
        }
        return dropList;
    }
}
