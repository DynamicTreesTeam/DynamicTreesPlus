package com.dtteam.dynamictreesplus.systems.growthlogic;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;

public class DTPGrowthLogicKits {

    public static final StraightLogic STRAIGHT_LOGIC = new StraightLogic(DynamicTreesPlus.location("straight"));
    public static final SaguaroCactusLogic SAGUARO_CACTUS_LOGIC = new SaguaroCactusLogic(DynamicTreesPlus.location("saguaro_cactus"));
    public static final MegaCactusLogic MEGA_CACTUS_LOGIC = new MegaCactusLogic(DynamicTreesPlus.location("mega_cactus"));

    public static void register(final Registry<GrowthLogicKit> registry) {
        registry.registerAll(STRAIGHT_LOGIC, SAGUARO_CACTUS_LOGIC, MEGA_CACTUS_LOGIC);
    }

}
