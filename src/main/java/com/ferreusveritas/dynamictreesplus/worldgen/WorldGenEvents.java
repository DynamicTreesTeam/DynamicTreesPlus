package com.ferreusveritas.dynamictreesplus.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeFeatureCancellerRegistry;
import com.ferreusveritas.dynamictreesplus.worldgen.canceller.CactusFeatureCanceller;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldGenEvents {

    public static final String CACTUS_CANCELLER = "cactus";

    @SubscribeEvent
    public void onTreeFeatureCancelRegistry(TreeFeatureCancellerRegistry.TreeFeatureCancellerRegistryEvent event) {
        final TreeFeatureCancellerRegistry registry = event.getFeatureCancellerRegistry();

        // This registers the tree feature canceller for cacti, which will cancel any BlockCluster features using the CactusBlock class.
        registry.register(CACTUS_CANCELLER, new CactusFeatureCanceller<>(CactusBlock.class));
    }

    @SubscribeEvent
    public void onTreeCancelRegistry(TreeCancelRegistryEvent event) {
        final ITreeCanceller treeCanceller = event.getTreeCanceller();
        final List<String> namespaces = Collections.singletonList(DynamicTrees.MINECRAFT);
        // Gets a list of all vanilla Minecraft biome registry keys.
        final List<RegistryKey<Biome>> vanillaBiomes = ForgeRegistries.BIOMES.getEntries().stream().map(Map.Entry::getKey)
                .filter(key -> key.getLocation().getNamespace().equals(DynamicTrees.MINECRAFT)).collect(Collectors.toList());

        // This registers the cancellation of all cactus features with the namespace "minecraft" from all sandy biomes with the namespace "minecraft".
        vanillaBiomes.stream().filter(key -> BiomeDictionary.hasType(key, BiomeDictionary.Type.SANDY)).forEach(key ->
                treeCanceller.register(key.getLocation(), namespaces, Collections.singletonList(CACTUS_CANCELLER)));
    }

}
