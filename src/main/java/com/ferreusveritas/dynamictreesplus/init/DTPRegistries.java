package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.systems.featuregen.CactusClonesGenFeature;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.CactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.MegaCactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.SaguaroCactusLogic;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static final CactusLogic PILLAR_LOGIC = new CactusLogic(DynamicTreesPlus.resLoc("pillar_cactus"), 5);
    public static final CactusLogic PIPE_LOGIC = new CactusLogic(DynamicTreesPlus.resLoc("pipe_cactus"), 3);
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.resLoc("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.resLoc("mega_cactus"));

    @SubscribeEvent
    public static void onGrowthLogicRegistry(final RegistryEvent.Register<GrowthLogicKit> event) {
        event.getRegistry().registerAll(PILLAR_LOGIC, PIPE_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
    }

    public static final GenFeature CACTUS_CLONES = new CactusClonesGenFeature(DynamicTreesPlus.resLoc("cactus_clones"));

    @SubscribeEvent
    public static void onGenFeatureRegistry(final RegistryEvent.Register<GenFeature> event) {
        event.getRegistry().registerAll(CACTUS_CLONES);
    }

}
