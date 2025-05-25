package com.dtteam.dynamictreesplus.resources;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.deserializer.ConfiguredDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.RegistryEntryDeserializer;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;

public class DTPJsonDeserializers {

    public static JsonDeserializer<MushroomShapeConfiguration> CONFIGURED_MUSHROOM_SHAPE_KIT;
    public static JsonDeserializer<CapProperties> CAP_PROPERTIES;

    public static void register (){

        CONFIGURED_MUSHROOM_SHAPE_KIT = JsonDeserializers.register(MushroomShapeConfiguration.class,
                new ConfiguredDeserializer<>("Mushroom Shape Kit", MushroomShapeKit.class,
                        MushroomShapeConfiguration.TEMPLATES));

        CAP_PROPERTIES = JsonDeserializers.register(CapProperties.class,
                new RegistryEntryDeserializer<>(CapProperties.REGISTRY));

    }

}
