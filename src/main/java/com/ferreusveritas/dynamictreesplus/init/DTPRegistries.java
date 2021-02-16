package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictreesplus.trees.Cactus;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static Cactus dynamicCactus;

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

        dynamicCactus = new Cactus();
        dynamicCactus.registerSpecies(Species.REGISTRY);

        ArrayList<Block> treeBlocks = new ArrayList<>();
        dynamicCactus.getRegisterableBlocks(treeBlocks);

        registry.registerAll(treeBlocks.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();

        ArrayList<Item> treeItems = new ArrayList<>();
        dynamicCactus.getRegisterableItems(treeItems);

        registry.registerAll(treeItems.toArray(new Item[0]));
    }


}
