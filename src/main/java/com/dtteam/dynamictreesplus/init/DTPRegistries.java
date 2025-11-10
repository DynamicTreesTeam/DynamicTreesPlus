package com.dtteam.dynamictreesplus.init;

import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.block.CommonVoxelShapes;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.event.AddResourceLoadersEvent;
import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.event.TypeRegistryEvent;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusFruit;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.resources.CapPropertiesResourceLoader;
import com.dtteam.dynamictreesplus.systems.featuregen.DynamicTreesPlusGenFeatures;
import com.dtteam.dynamictreesplus.systems.growthlogic.MegaCactusLogic;
import com.dtteam.dynamictreesplus.systems.growthlogic.SaguaroCactusLogic;
import com.dtteam.dynamictreesplus.systems.growthlogic.StraightLogic;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKits;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogicKits;
import com.dtteam.dynamictreesplus.tree.CactusFamily;
import com.dtteam.dynamictreesplus.tree.CactusSpecies;
import com.dtteam.dynamictreesplus.tree.HugeMushroomFamily;
import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import com.dtteam.dynamictreesplus.worldgen.canceller.CactusFeatureCanceller;
import com.dtteam.dynamictreesplus.worldgen.canceller.MushroomFeatureCanceller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = DynamicTreesPlus.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static final StraightLogic STRAIGHT_LOGIC = new StraightLogic(DynamicTreesPlus.location("straight"));
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.location("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.location("mega_cactus"));

    public static VoxelShape TALL_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.6875f, 0.625f));
    public static VoxelShape MEDIUM_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.5625f, 0.625f));
    public static VoxelShape SHORT_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.4375f, 0.625f));


    public static void setup() {
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("tall_cactus").toString(), TALL_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("medium_cactus").toString(), MEDIUM_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("short_cactus").toString(), SHORT_CACTUS_SAPLING_SHAPE);

    }

    @SubscribeEvent
    public static void registerGrowthLogic(final RegistryEvent<GrowthLogicKit> event) {
        if (event.isEntryOfType(GrowthLogicKit.class)){
            event.getRegistry().registerAll(STRAIGHT_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
        }
    }

    @SubscribeEvent
    public static void registerCactusThicknessLogic(final RegistryEvent<CactusThicknessLogic> event) {
        if (event.isEntryOfType(CactusThicknessLogic.class)){
            event.getRegistry().registerAll(CactusThicknessLogicKits.PILLAR, CactusThicknessLogicKits.PIPE, CactusThicknessLogicKits.SAGUARO, CactusThicknessLogicKits.MEGA);
        }
    }

    @SubscribeEvent
    public static void registerGenFeature(final RegistryEvent<GenFeature> event) {
        if (event.isEntryOfType(GenFeature.class)){
            DynamicTreesPlusGenFeatures.registerGenFeatures(event);
        }
    }
    @SubscribeEvent
    public static void registerFruitType(final TypeRegistryEvent<Fruit> event) {
        if (event.isEntryOfType(Fruit.class)){
            event.registerType(DynamicTreesPlus.location("cactus_fruit"), CactusFruit.TYPE);
        }
    }

    @SubscribeEvent
    public static void registerFamilyType(final TypeRegistryEvent<Family> event) {
        if (event.isEntryOfType(Family.class)){
            event.registerType(DynamicTreesPlus.CACTUS, CactusFamily.TYPE);
            event.registerType(DynamicTreesPlus.MUSHROOM, HugeMushroomFamily.TYPE);
        }
    }

    @SubscribeEvent
    public static void registerSpeciesType(final TypeRegistryEvent<Species> event) {
        if (event.isEntryOfType(Species.class)){
            event.registerType(DynamicTreesPlus.CACTUS, CactusSpecies.TYPE);
            event.registerType(DynamicTreesPlus.MUSHROOM, HugeMushroomSpecies.TYPE);
        }
    }

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        if (event.isEntryOfType(FeatureCanceller.class)) {
            event.getRegistry().registerAll(new CactusFeatureCanceller<>(DynamicTreesPlus.location("cactus"), CactusBlock.class));
            event.getRegistry().registerAll(new MushroomFeatureCanceller<>(DynamicTreesPlus.location("mushroom"), HugeMushroomFeatureConfiguration.class));
        }
    }

    @SubscribeEvent
    public static void onMushroomShapeKitRegistry(final RegistryEvent<MushroomShapeKit> event) {
        if (event.isEntryOfType(MushroomShapeKit.class)){
            MushroomShapeKits.register(event.getRegistry());
        }
    }

    @SubscribeEvent
    public static void addResourceLoaders(AddResourceLoadersEvent.Pre event){
        event.getResourceManager().addLoader(CapPropertiesResourceLoader.CAP_PROPERTIES_LOADER);
        event.getResourceManager().addLoader(CapPropertiesResourceLoader.MUSHROOM_SHAPE_KIT_TEMPLATE_LOADER);
    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        CactusThicknessLogic.REGISTRY.postRegistryEvent();
        MushroomShapeKit.REGISTRY.postRegistryEvent();
        CapProperties.REGISTRY.postRegistryEvent();
    }

    @SubscribeEvent
    public static void loadResources(RegisterEvent event) {
        if (event.getRegistryKey() == BuiltInRegistries.BLOCK.key()) {
            CactusThicknessLogic.REGISTRY.lock();
            MushroomShapeKit.REGISTRY.lock();
            CapProperties.REGISTRY.lock();
        }
    }

}
