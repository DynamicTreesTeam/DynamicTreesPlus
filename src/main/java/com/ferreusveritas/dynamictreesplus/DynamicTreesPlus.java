package com.ferreusveritas.dynamictreesplus;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.trees.FamilyType;
import com.ferreusveritas.dynamictrees.trees.SpeciesType;
import com.ferreusveritas.dynamictreesplus.init.DTPClient;
import com.ferreusveritas.dynamictreesplus.init.DTPConfigs;
import com.ferreusveritas.dynamictreesplus.resources.JsonRegistries;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogicKits;
import com.ferreusveritas.dynamictreesplus.trees.CactusFamily;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import com.ferreusveritas.dynamictreesplus.worldgen.WorldGenEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";

    public DynamicTreesPlus () {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        if (DTConfigs.worldGen.get()) {
            MinecraftForge.EVENT_BUS.register(new WorldGenEvents());
        }

        CactusThicknessLogicKits.register();

        FamilyType.register(CactusFamily.CACTUS_FAMILY);
        SpeciesType.register(CactusSpecies.CACTUS_SPECIES);

        JsonRegistries.registerJsonGetters();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTPClient.setup();
    }

    public static ResourceLocation resLoc (final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
