package com.dtteam.dynamictreesplus.resources;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.deserializer.ConfiguredDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.EnumDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.RegistryEntryDeserializer;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;

public class DTPJsonDeserializers {

    public static JsonDeserializer<CactusBranchBlock.CactusThickness> CACTUS_THICKNESS;
    public static JsonDeserializer<CactusThicknessLogic> CACTUS_THICKNESS_LOGIC;
    public static JsonDeserializer<MushroomShapeKit> MUSHROOM_SHAPE_KIT;
    public static JsonDeserializer<MushroomShapeConfiguration> CONFIGURED_MUSHROOM_SHAPE_KIT;
    public static JsonDeserializer<CapProperties> CAP_PROPERTIES;

    public static void register (){

        CACTUS_THICKNESS = JsonDeserializers.register(CactusBranchBlock.CactusThickness.class,
                        new EnumDeserializer<>(CactusBranchBlock.CactusThickness.class));

        CACTUS_THICKNESS_LOGIC = JsonDeserializers.register(CactusThicknessLogic.class,
                        new RegistryEntryDeserializer<>(CactusThicknessLogic.REGISTRY));

        MUSHROOM_SHAPE_KIT = JsonDeserializers.register(MushroomShapeKit.class,
                new RegistryEntryDeserializer<>(MushroomShapeKit.REGISTRY));

        CONFIGURED_MUSHROOM_SHAPE_KIT = JsonDeserializers.register(MushroomShapeConfiguration.class,
                new ConfiguredDeserializer<>("Mushroom Shape Kit", MushroomShapeKit.class,
                        MushroomShapeConfiguration.TEMPLATES));

        CAP_PROPERTIES = JsonDeserializers.register(CapProperties.class,
                new RegistryEntryDeserializer<>(CapProperties.REGISTRY));

    }

}
