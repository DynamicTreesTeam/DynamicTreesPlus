package com.ferreusveritas.dynamictreesplus;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictreesplus.init.DTPClient;
import com.ferreusveritas.dynamictreesplus.worldgen.WorldGenEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";

    public DynamicTreesPlus (){

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        if(DTConfigs.worldGen.get()) {
            MinecraftForge.EVENT_BUS.register(new WorldGenEvents());
        }

        MinecraftForge.EVENT_BUS.register(this);

    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTPClient.setup();
    }

}
