package com.ferreusveritas.dynamictreesplus.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

        ArrayList<Block> treeBlocks = new ArrayList<>();
        DTPTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);

        registry.registerAll(treeBlocks.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();

        ArrayList<Item> treeItems = new ArrayList<>();
        DTPTrees.dynamicCactus.getRegisterableItems(treeItems);

        registry.registerAll(treeItems.toArray(new Item[0]));
    }


}
