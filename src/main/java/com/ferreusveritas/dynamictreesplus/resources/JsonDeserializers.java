package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.deserialisation.ConfiguredDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.RegistryEntryDeserialiser;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;

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
