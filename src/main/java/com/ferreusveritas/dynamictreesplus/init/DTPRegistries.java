package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.api.registry.TypeRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.systems.dropcreators.DTPDropCreators;
import com.ferreusveritas.dynamictreesplus.systems.featuregen.CactusClonesGenFeature;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.CactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.MegaCactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.growthlogic.SaguaroCactusLogic;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.ferreusveritas.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogicKits;
import com.ferreusveritas.dynamictreesplus.trees.CactusFamily;
import com.ferreusveritas.dynamictreesplus.trees.CactusSpecies;
import com.ferreusveritas.dynamictreesplus.worldgen.canceller.CactusFeatureCanceller;
import net.minecraft.block.CactusBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static final CactusLogic CACTUS_LOGIC = new CactusLogic(DynamicTreesPlus.resLoc("cactus"));
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.resLoc("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.resLoc("mega_cactus"));

    public static VoxelShape TALL_CACTUS_SAPLING_SHAPE = VoxelShapes.create(
            new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.6875f, 0.625f));
    public static VoxelShape MEDIUM_CACTUS_SAPLING_SHAPE = VoxelShapes.create(
            new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.5625f, 0.625f));
    public static VoxelShape SHORT_CACTUS_SAPLING_SHAPE = VoxelShapes.create(
            new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.4375f, 0.625f));


    public static void setup(){
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.resLoc("tall_cactus").toString(), TALL_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.resLoc("medium_cactus").toString(), MEDIUM_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.resLoc("short_cactus").toString(), SHORT_CACTUS_SAPLING_SHAPE);

    }

    @SubscribeEvent
    public static void registerGrowthLogic(final RegistryEvent<GrowthLogicKit> event) {
        event.getRegistry().registerAll(CACTUS_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
    }

    @SubscribeEvent
    public static void registerCactusThicknessLogic(final RegistryEvent<CactusThicknessLogic> event) {
        event.getRegistry().registerAll(CactusThicknessLogicKits.PILLAR, CactusThicknessLogicKits.PIPE, CactusThicknessLogicKits.SAGUARO, CactusThicknessLogicKits.MEGA);
    }

    public static final GenFeature CACTUS_CLONES = new CactusClonesGenFeature(DynamicTreesPlus.resLoc("cactus_clones"));

    @SubscribeEvent
    public static void registerGenFeature(final RegistryEvent<GenFeature> event) {
        event.getRegistry().registerAll(CACTUS_CLONES);
    }

    public static final ResourceLocation CACTUS = DynamicTreesPlus.resLoc("cactus");

    @SubscribeEvent
    public static void registerFamilyType(final TypeRegistryEvent<Family> event) {
        event.registerType(CACTUS, CactusFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesType(final TypeRegistryEvent<Species> event) {
        event.registerType(CACTUS, CactusSpecies.TYPE);
    }

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(new CactusFeatureCanceller<>(DynamicTreesPlus.resLoc("cactus"), CactusBlock.class));
    }

    @SubscribeEvent
    public static void onDropCreatorRegistry (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<DropCreator> event) {
        DTPDropCreators.register(event.getRegistry());
    }

}
