package com.ferreusveritas.dynamictreesplus.worldgen;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeFeatureCancellerRegistry;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.worldgen.canceller.CactusFeatureCanceller;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldGenEvents {

    @SubscribeEvent
    public void onBiomeDataBasePopulatorRegistry(WorldGenRegistry.BiomeDataBasePopulatorRegistryEvent event){

        event.register(new BiomeDataBasePopulatorJson(new ResourceLocation(DynamicTreesPlus.MOD_ID, "worldgen/default.json")));

    }

    @SubscribeEvent
    public void onTreeFeatureCancelRegistry(TreeFeatureCancellerRegistry.TreeFeatureCancellerRegistryEvent event) {
        final TreeFeatureCancellerRegistry registry = event.getFeatureCancellerRegistry();

        // This registers the tree feature canceller for cacti, which will cancel any BlockCluster features using the CactusBlock class.
        registry.register(TreeFeatureCancellerRegistry.CACTUS_CANCELLER, new CactusFeatureCanceller<>(CactusBlock.class));

    }

}
