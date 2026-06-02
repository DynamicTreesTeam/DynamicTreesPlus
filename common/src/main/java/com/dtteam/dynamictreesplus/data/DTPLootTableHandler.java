package com.dtteam.dynamictreesplus.data;

import com.dtteam.dynamictrees.loot.DTLootParameterSets;
import com.dtteam.dynamictrees.loot.function.MultiplyByLogsCount;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
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

import java.util.List;

public class DTPLootTableHandler {

    protected static LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))));
    }

    protected static Holder<Enchantment> getFortune(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return registrylookup.getOrThrow(Enchantments.SILK_TOUCH);
    }

    public static LootTable.Builder createMushroomBranchDrops(Block primitiveLogBlock, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyByLogsCount.multiplyByLogsCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                                .when(hasSilkTouch(registries))
                )
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createCapBlockDrops(Block primitiveCapBlock, Item primitiveSapling, int countMin, int countMax, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(primitiveCapBlock)
                                        .when(hasSilkTouch(registries)),
                                LootItem.lootTableItem(primitiveSapling)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(countMin, countMax),false))
                                        .apply(LimitCount.limitCount(IntRange.lowerBound(0)))
                                        .apply(ApplyExplosionDecay.explosionDecay())
                        ))
        ).setParamSet(LootContextParamSets.BLOCK);
    }

    public static LootTable.Builder createCapDrops(Block primitiveCapBlock, Item primitiveSapling, LootContextParamSet parameterSet, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                                AlternativesEntry.alternatives(
                                        LootItem.lootTableItem(primitiveCapBlock).when(hasSilkTouch(registries)),
                                        LootItem.lootTableItem(primitiveSapling))
                        ).apply(ApplyExplosionDecay.explosionDecay())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                getFortune(registries), 0.2F, 0.2333333F, 0.2666666F, 0.3F
                        ))
        ).setParamSet(parameterSet);
    }


}
