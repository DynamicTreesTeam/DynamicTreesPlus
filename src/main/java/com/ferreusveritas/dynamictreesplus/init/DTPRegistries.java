package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RegistryEvent;
import com.ferreusveritas.dynamictrees.util.TypeRegistryEvent;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.systems.featuregen.CactusClonesGenFeature;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.CactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.MegaCactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.SaguaroCactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogicKits;
import com.ferreusveritas.dynamictreesplus.trees.CactusFamily;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static final CactusLogic PILLAR_LOGIC = new CactusLogic(DynamicTreesPlus.resLoc("pillar_cactus"), 5);
    public static final CactusLogic PIPE_LOGIC = new CactusLogic(DynamicTreesPlus.resLoc("pipe_cactus"), 3);
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.resLoc("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.resLoc("mega_cactus"));

    @SubscribeEvent
    public static void registerGrowthLogic(final RegistryEvent<GrowthLogicKit> event) {
        event.getRegistry().registerAll(PILLAR_LOGIC, PIPE_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
    }

    @SubscribeEvent
    public static void registerCactusThicknessLogic(final RegistryEvent<CactusThicknessLogic> event) {
        event.getRegistry().registerAll(CactusThicknessLogicKits.PILLAR, CactusThicknessLogicKits.PIPE, CactusThicknessLogicKits.SAGUARO, CactusThicknessLogicKits.MEGA);
    }

    public static final GenFeature CACTUS_CLONES = new CactusClonesGenFeature(DynamicTreesPlus.resLoc("cactus_clones"));

    @SubscribeEvent
    public static void registerGenFeature(final RegistryEvent<GenFeature> event) {
        event.getRegistry().registerAll(CACTUS_CLONES);
    }

    @SubscribeEvent
    public static void registerFamilyType(final TypeRegistryEvent<Family> event) {
        event.registerType(DynamicTreesPlus.resLoc("cactus"), new CactusFamily.Type());
    }

    @SubscribeEvent
    public static void registerSpeciesType(final TypeRegistryEvent<Species> event) {
        event.registerType(DynamicTreesPlus.resLoc("cactus"), new CactusSpecies.Type());
    }

}
