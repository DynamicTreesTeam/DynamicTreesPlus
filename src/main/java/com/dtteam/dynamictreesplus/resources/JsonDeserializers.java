package com.dtteam.dynamictreesplus.resources;

import com.dtteam.dynamictrees.deserialisation.ConfiguredDeserialiser;
import com.dtteam.dynamictrees.deserialisation.JsonDeserialiser;
import com.dtteam.dynamictrees.deserialisation.JsonDeserialisers;
import com.dtteam.dynamictrees.deserialisation.RegistryEntryDeserialiser;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;

public class JsonDeserializers {

    public static JsonDeserialiser<MushroomShapeConfiguration> CONFIGURED_MUSHROOM_SHAPE_KIT;
    public static JsonDeserialiser<CapProperties> CAP_PROPERTIES;

    public static void register (){

        CONFIGURED_MUSHROOM_SHAPE_KIT = JsonDeserialisers.register(MushroomShapeConfiguration.class,
                new ConfiguredDeserialiser<>("Mushroom Shape Kit", MushroomShapeKit.class,
                        MushroomShapeConfiguration.TEMPLATES));

        CAP_PROPERTIES = JsonDeserialisers.register(CapProperties.class,
                new RegistryEntryDeserialiser<>(CapProperties.REGISTRY));

    }

}
