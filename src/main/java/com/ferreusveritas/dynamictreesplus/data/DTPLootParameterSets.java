package com.ferreusveritas.dynamictreesplus.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

//public class DTPLootParameterSets {
//
//    public static final LootContextParamSet MUSHROOM_CAP = register("mushroom_cap", builder ->
//            builder.required(LootContextParams.BLOCK_STATE)
//                    .required(DTLootContextParams.SPECIES)
//                    .required(LootContextParams.TOOL)
//                    .optional(LootContextParams.EXPLOSION_RADIUS)
//    );
//
//    private static LootContextParamSet register(String path, Consumer<LootContextParamSet.Builder> builderConsumer) {
//        final LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
//        builderConsumer.accept(builder);
//
//        final LootContextParamSet paramSet = builder.build();
//        LootContextParamSets.REGISTRY.put(DynamicTrees.location(path), paramSet);
//
//        return paramSet;
//    }
//
//}
