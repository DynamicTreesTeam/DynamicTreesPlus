package com.dtteam.dynamictreesplus;


import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.data.DTPDataGenerators;
import com.dtteam.dynamictreesplus.init.DTPConfigs;
import com.dtteam.dynamictreesplus.resources.DTPShapes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(DynamicTreesPlus.MOD_ID)
public class DynamicTreesPlusNeoForge {

    public DynamicTreesPlusNeoForge(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::gatherData);

        modContainer.registerConfig(ModConfig.Type.SERVER, DTPConfigs.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.COMMON, DTPConfigs.COMMON_CONFIG);

        DTPShapes.setup();
        DTPDataGenerators.register();

        NeoForgeRegistryHandler.setup(DynamicTreesPlus.MOD_ID, modBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // VillageCactusReplacement.replaceCactiFromVanillaVillages();
    }

    private void gatherData(final GatherDataEvent.Server event) {
//        BranchLoaderBuilder.branchBuilders.put(
//                DynamicTreesPlus.CACTUS,  (parent, existingFileHelper) ->
//                        new BranchLoaderBuilder(DynamicTreesPlus.CACTUS, parent, existingFileHelper));
//
//        Resources.MANAGER.gatherData();
//        GatherDataHelper.gatherAllData(DynamicTreesPlus.MOD_ID, event,
//                SoilProperties.REGISTRY,
//                Family.REGISTRY,
//                Species.REGISTRY,
//                LeavesProperties.REGISTRY,
//                CapProperties.REGISTRY
//        );
    }

}
