package com.dtteam.dynamictreesplus;

import com.dtteam.dynamictreesplus.event.DTPCommonEventHandler;
import com.dtteam.dynamictreesplus.event.DTPRegistryHandler;
import com.dtteam.dynamictreesplus.init.DTPConfigs;
import com.dtteam.dynamictreesplus.resources.DTPShapes;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public class DynamicTreesPlusFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        NeoForgeConfigRegistry.INSTANCE.register(DynamicTreesPlus.MOD_ID, ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);
        NeoForgeConfigRegistry.INSTANCE.register(DynamicTreesPlus.MOD_ID, ModConfig.Type.COMMON, DTPConfigs.COMMON_CONFIG);

        DTPShapes.setup();

        DTPCommonEventHandler.RegisterEvents();
        DTPRegistryHandler.LockRegistries();

    }
}
