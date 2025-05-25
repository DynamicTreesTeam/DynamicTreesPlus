package com.dtteam.dynamictreesplus;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.data.builder.BranchLoaderBuilder;
import com.dtteam.dynamictrees.loot.DTLoot;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.init.DTPConfigs;
import com.dtteam.dynamictreesplus.init.DTPRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";
    public static final ResourceLocation CACTUS = DynamicTreesPlus.location("cactus");
    public static final ResourceLocation MUSHROOM = DynamicTreesPlus.location("mushroom");

    public DynamicTreesPlus(IEventBus modBus, ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::gatherData);

        NeoForgeRegistryHandler.setup(MOD_ID, modBus);

        DTPRegistries.setup();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // VillageCactusReplacement.replaceCactiFromVanillaVillages();
        DTLoot.load();
        DynamicTreeFeature.setup();

        // Clears and locks registry handlers to free them from memory.
        RegistryHandler.REGISTRY.clear();

        Resources.MANAGER.setup();
    }

    private void gatherData(final GatherDataEvent event) {
        BranchLoaderBuilder.branchBuilders.put(
                CACTUS,  (parent, existingFileHelper) ->
                        new BranchLoaderBuilder(CACTUS, parent, existingFileHelper));

        Resources.MANAGER.gatherData();
        GatherDataHelper.gatherAllData(MOD_ID, event,
                SoilProperties.REGISTRY,
                Family.REGISTRY,
                Species.REGISTRY,
                LeavesProperties.REGISTRY,
                CapProperties.REGISTRY
        );
    }

    public static ResourceLocation location(final String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

}
