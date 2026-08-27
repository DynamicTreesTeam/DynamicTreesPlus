//package com.dtteam.dynamictreesplus.event;
//
//import com.dtteam.dynamictreesplus.DynamicTreesPlus;
//import com.dtteam.dynamictreesplus.DynamicTreesPlusNeoForge;
//import com.dtteam.dynamictreesplus.model.loader.CactusBlockModelLoader;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.ModelEvent;
//
///**
// * @author Harley O'Connor
// */
//@EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, value = Dist.CLIENT)
//public final class BakedModelEventHandler {
//
//    @SubscribeEvent
//    public static void onModelRegistryEvent(ModelEvent.RegisterLoaders event) {
//        event.register(DynamicTreesPlus.CACTUS, new CactusBlockModelLoader());
//    }
//
//}
