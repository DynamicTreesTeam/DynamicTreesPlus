package com.ferreusveritas.dynamictreesplus.event;

import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.models.loaders.CactusBlockModelLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        // Register model loaders for baked models.
        ModelLoaderRegistry.registerLoader(DynamicTreesPlus.resLoc("cactus"), new CactusBlockModelLoader());
    }

}
