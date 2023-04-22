package com.ferreusveritas.dynamictreesplus;

import com.ferreusveritas.dynamictrees.api.GatherDataHelper;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.init.DTPClient;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import com.ferreusveritas.dynamictreesplus.init.DTPRegistries;
import com.ferreusveritas.dynamictreesplus.worldgen.structure.VillageCactusReplacement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";

    public DynamicTreesPlus() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::gatherData);

        RegistryHandler.setup(MOD_ID);

        DTPRegistries.setup();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTPClient.setup();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (DTConfigs.CANCEL_VANILLA_VILLAGE_TREES.get()) {
            VillageCactusReplacement.replaceCactiFromVanillaVillages();
        }
    }

    private void gatherData(final GatherDataEvent event) {
        GatherDataHelper.gatherAllData(
                MOD_ID,
                event,
                SoilProperties.REGISTRY,
                Family.REGISTRY,
                Species.REGISTRY,
                LeavesProperties.REGISTRY,
                CapProperties.REGISTRY
        );
    }

    public static ResourceLocation location(final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
