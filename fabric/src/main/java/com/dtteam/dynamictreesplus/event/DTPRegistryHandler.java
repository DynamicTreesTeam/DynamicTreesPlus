package com.dtteam.dynamictreesplus.event;

import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.dtteam.dynamictreesplus.systems.thicknesslogic.CactusThicknessLogic;

public class DTPRegistryHandler {

    public static void LockRegistries(){
        CactusThicknessLogic.REGISTRY.postRegistryEvent();
        MushroomShapeKit.REGISTRY.postRegistryEvent();
        CapProperties.REGISTRY.postRegistryEvent();

        CactusThicknessLogic.REGISTRY.lock();
        MushroomShapeKit.REGISTRY.lock();
        CapProperties.REGISTRY.lock();
    }

}
