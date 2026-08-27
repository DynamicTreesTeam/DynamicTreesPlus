//package com.dtteam.dynamictreesplus.event;
//
//import com.dtteam.dynamictrees.event.DataGenerationStreamEvent;
//import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.Identifier;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.server.packs.PackType;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.common.data.ExistingFileHelper;
//
//import java.util.Map;
//
//@EventBusSubscriber()
//public class GenerateLootTableHandler {
//
//    @SubscribeEvent
//    public static void onLootTableProviderGenerate(final DataGenerationStreamEvent event) {
//        CapProperties.REGISTRY.dataGenerationStream(event.getModId()).forEach(capProperties -> {
//            addCapBlockTable(capProperties, event.getFileHelper(), event.getMap(), event.getRegistries());
//            addCapTable(capProperties, event.getFileHelper(), event.getMap(), event.getRegistries());
//        });
//    }
//
//    private static void addCapBlockTable(CapProperties capProperties, ExistingFileHelper existingFileHelper, Map<ResourceKey<LootTable>, LootTable.Builder> map, HolderLookup.Provider registries) {
//        if (capProperties.shouldGenerateBlockDrops()) {
//            final Identifier capBlockTablePath = capProperties.getBlockLootTableName();
//            if (!existingFileHelper.exists(capBlockTablePath, PackType.SERVER_DATA)) {
//                map.put(ResourceKey.create(Registries.LOOT_TABLE, capBlockTablePath), capProperties.createBlockDrops(registries));
//            }
//        }
//    }
//
//    private static void addCapTable(CapProperties capProperties, ExistingFileHelper existingFileHelper, Map<ResourceKey<LootTable>, LootTable.Builder> map, HolderLookup.Provider registries) {
//        if (capProperties.shouldGenerateDrops()) {
//            final Identifier capTablePath = capProperties.getLootTableName();
//            if (!existingFileHelper.exists(capTablePath, PackType.SERVER_DATA)) {
//                map.put(ResourceKey.create(Registries.LOOT_TABLE, capTablePath), capProperties.createDrops(registries));
//            }
//        }
//    }
//
//}
