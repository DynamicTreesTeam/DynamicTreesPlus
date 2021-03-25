package com.ferreusveritas.dynamictreesplus;

import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictreesplus.init.DTPClient;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";

    public DynamicTreesPlus () {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);

        modBus.addListener(this::clientSetup);

        final RegistryHandler registryHandler = new RegistryHandler(MOD_ID);
        RegistryHandler.REGISTRY.register(registryHandler);
        modBus.register(registryHandler);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTPClient.setup();
    }

    public static ResourceLocation resLoc (final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
