package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.json.EnumGetter;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.RegistryEntryGetter;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JsonRegistries {

    @SubscribeEvent
    public static void registerAppliers(final JsonApplierRegistryEvent<Species> event) {
        // Add to reload appliers only.
        if (!event.isReloadApplier())
            return;

        registerJsonGetters();

        event.getApplierList().register("seed_per_branch", CactusSpecies.class, Float.class, CactusSpecies::setSeedPerBranch)
                .register("cactus_thickness_logic", CactusSpecies.class, CactusThicknessLogic.class, CactusSpecies::setThicknessLogic);
    }

    public static void registerJsonGetters () {
        // Register getter for cactus thickness logic.
        JsonObjectGetters.register(CactusThicknessLogic.class, new RegistryEntryGetter<>(CactusThicknessLogic.REGISTRY, "cactus thickness logic"));

        // Register getter for cactus thickness enum.
        JsonObjectGetters.register(CactusBranchBlock.CactusThickness.class, new EnumGetter<>(CactusBranchBlock.CactusThickness.class));
    }

}
