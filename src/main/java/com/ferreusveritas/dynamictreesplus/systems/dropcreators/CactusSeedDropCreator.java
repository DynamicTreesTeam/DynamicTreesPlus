package com.ferreusveritas.dynamictreesplus.systems.dropcreators;

import com.ferreusveritas.dynamictrees.systems.dropcreators.ConfiguredDropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
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
 * @author ferreusveritas
 */
public class CactusSeedDropCreator extends DropCreator {

    private float seedPerBranch = 0.5f;

    public CactusSeedDropCreator() {
        super(DynamicTreesPlus.resLoc("cactus_seeds"));
    }

    @Override
    protected void registerProperties() {

    }

    public CactusSeedDropCreator setSeedPerBranch(float seedPerLog) {
        this.seedPerBranch = seedPerLog;
        return this;
    }

    @Override
    public void appendLogDrops(ConfiguredDropCreator<DropCreator> configuration, LogDropContext context) {
        int numLogs = (int) (context.volume().getVolume() * this.seedPerBranch);
        while (numLogs > 0) {
            context.drops().add(context.species().getSeedStack(Math.min(numLogs, 64)));
            numLogs -= 64;
        }
        super.appendLogDrops(configuration, context);
    }

}
