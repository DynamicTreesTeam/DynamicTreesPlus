package com.dtteam.dynamictreesplus.data;

import com.dtteam.dynamictrees.data.provider.DataGenerationStreamEvent;
import com.dtteam.dynamictrees.loot.DTLootParameterSets;
import com.dtteam.dynamictrees.loot.function.MultiplyLogsCount;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTPLootTableHandler {
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));

    @SubscribeEvent
    public static void onLootTableProviderGenerate(final DataGenerationStreamEvent event) {
        CapProperties.REGISTRY.dataGenerationStream(event.getModId()).forEach(capProperties -> {
            addCapBlockTable(capProperties, event.getExistingFileHelper(), event.getMap());
            addCapTable(capProperties, event.getExistingFileHelper(), event.getMap());
        });
    }

    private static void addCapBlockTable(CapProperties capProperties, ExistingFileHelper existingFileHelper, Map<ResourceLocation, LootTable.Builder> map) {
        if (capProperties.shouldGenerateBlockDrops()) {
            final ResourceLocation capBlockTablePath = capProperties.getBlockLootTableName();
            if (!existingFileHelper.exists(capBlockTablePath, PackType.SERVER_DATA)) {
                map.put(capBlockTablePath, capProperties.createBlockDrops());
            }
        }
    }

    private static void addCapTable(CapProperties capProperties, ExistingFileHelper existingFileHelper, Map<ResourceLocation, LootTable.Builder> map) {
        if (capProperties.shouldGenerateDrops()) {
            final ResourceLocation capTablePath = capProperties.getLootTableName();
            if (!existingFileHelper.exists(capTablePath, PackType.SERVER_DATA)) {
                map.put(capTablePath, capProperties.createDrops());
            }
        }
    }

    public static LootTable.Builder createMushroomBranchDrops(Block primitiveLogBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyLogsCount.multiplyLogsCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                                .when(HAS_SILK_TOUCH)
                )
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createCapBlockDrops(Block primitiveCapBlock, Item primitiveSapling, int countMin, int countMax) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(primitiveCapBlock)
                                        .when(HAS_SILK_TOUCH),
                                LootItem.lootTableItem(primitiveSapling)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(countMin, countMax),false))
                                        .apply(LimitCount.limitCount(IntRange.lowerBound(0)))
                                        .apply(ApplyExplosionDecay.explosionDecay())
                        ))
        ).setParamSet(LootContextParamSets.BLOCK);
    }

    public static LootTable.Builder createCapDrops(Block primitiveCapBlock, Item primitiveSapling, LootContextParamSet parameterSet) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                                AlternativesEntry.alternatives(
                                        LootItem.lootTableItem(primitiveCapBlock).when(HAS_SILK_TOUCH),
                                        LootItem.lootTableItem(primitiveSapling))
                        ).apply(ApplyExplosionDecay.explosionDecay())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE, 0.2F, 0.2333333F, 0.2666666F, 0.3F
                        ))
        ).setParamSet(parameterSet);
    }


}
