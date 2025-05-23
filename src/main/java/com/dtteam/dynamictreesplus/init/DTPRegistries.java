package com.dtteam.dynamictreesplus.init;

import com.dtteam.dynamictrees.api.registry.RegistryEvent;
import com.dtteam.dynamictrees.api.registry.TypeRegistryEvent;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.resources.Resources;
import com.dtteam.dynamictrees.systems.fruit.Fruit;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.CommonVoxelShapes;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusFruit;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.resources.CapPropertiesResourceLoader;
import com.dtteam.dynamictreesplus.resources.JsonDeserializers;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTPRegistries {

    public static final StraightLogic STRAIGHT_LOGIC = new StraightLogic(DynamicTreesPlus.location("straight"));
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.location("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.location("mega_cactus"));

    public static VoxelShape TALL_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.6875f, 0.625f));
    public static VoxelShape MEDIUM_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.5625f, 0.625f));
    public static VoxelShape SHORT_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.4375f, 0.625f));


    public static void setup() {
        Resources.MANAGER.addLoader(CapPropertiesResourceLoader.CAP_PROPERTIES_LOADER);
        Resources.MANAGER.addLoader(CapPropertiesResourceLoader.MUSHROOM_SHAPE_KIT_TEMPLATE_LOADER);

        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("tall_cactus").toString(), TALL_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("medium_cactus").toString(), MEDIUM_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("short_cactus").toString(), SHORT_CACTUS_SAPLING_SHAPE);

    }

    @SubscribeEvent
    public static void registerGrowthLogic(final RegistryEvent<GrowthLogicKit> event) {
        event.getRegistry().registerAll(STRAIGHT_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
    }

    @SubscribeEvent
    public static void registerCactusThicknessLogic(final RegistryEvent<CactusThicknessLogic> event) {
        event.getRegistry().registerAll(CactusThicknessLogicKits.PILLAR, CactusThicknessLogicKits.PIPE, CactusThicknessLogicKits.SAGUARO, CactusThicknessLogicKits.MEGA);
    }

    @SubscribeEvent
    public static void registerGenFeature(final RegistryEvent<GenFeature> event) {
        DynamicTreesPlusGenFeatures.registerGenFeatures(event);
    }
    @SubscribeEvent
    public static void registerFruitType(final TypeRegistryEvent<Fruit> event) {
        event.registerType(DynamicTreesPlus.location("cactus_fruit"), CactusFruit.TYPE);
    }

    public static final ResourceLocation CACTUS = DynamicTreesPlus.location("cactus");
    public static final ResourceLocation MUSHROOM = DynamicTreesPlus.location("mushroom");

    @SubscribeEvent
    public static void registerFamilyType(final TypeRegistryEvent<Family> event) {
        event.registerType(CACTUS, CactusFamily.TYPE);
        event.registerType(MUSHROOM, HugeMushroomFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesType(final TypeRegistryEvent<Species> event) {
        event.registerType(CACTUS, CactusSpecies.TYPE);
        event.registerType(MUSHROOM, HugeMushroomSpecies.TYPE);
    }

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(new CactusFeatureCanceller<>(DynamicTreesPlus.location("cactus"), CactusBlock.class));
    }

    @SubscribeEvent
    public static void onMushroomShapeKitRegistry(final RegistryEvent<MushroomShapeKit> event) {
        MushroomShapeKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        JsonDeserializers.register();

        CapProperties.REGISTRY.postRegistryEvent();
        //MushroomShapeKit.REGISTRY.postRegistryEvent();
    }

    @SubscribeEvent
    public static void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
            // Lock the registries
            CapProperties.REGISTRY.lock();
            MushroomShapeKit.REGISTRY.lock();
        }
    }

}
