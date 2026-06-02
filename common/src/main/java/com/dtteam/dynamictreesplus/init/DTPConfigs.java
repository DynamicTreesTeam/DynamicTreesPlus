package com.dtteam.dynamictreesplus.init;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DTPConfigs {

    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec COMMON_CONFIG;
//    public static ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.BooleanValue CAN_BONE_MEAL_CACTUS;
    public static final ModConfigSpec.BooleanValue CACTUS_PRICKLE_ON_MOVE_ONLY;
    public static final ModConfigSpec.BooleanValue CACTUS_KILL_ITEMS;
    public static final ModConfigSpec.BooleanValue REPLACE_MUSHROOM_SAPLING_ON_PLACEMENT;
    public static final ModConfigSpec.BooleanValue REPLACE_MUSHROOM_SAPLING_ON_GROWTH;

    static {
        final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
        final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
//        final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        SERVER_BUILDER.comment("Cactus Settings").push("cactus");
        CAN_BONE_MEAL_CACTUS = SERVER_BUILDER.comment("If enabled bone meal can be used to speed up cactus growth.").
                define("canBoneMealCactus", false);
        CACTUS_PRICKLE_ON_MOVE_ONLY = SERVER_BUILDER.comment("If enabled dynamic cacti will only prickle when moving (similar to how berry bushes prickle)").
                define("cactusPrickleOnMoveOnly", true);
        CACTUS_KILL_ITEMS = SERVER_BUILDER.comment("If enabled dynamic cacti will destroy items on contact").
                define("cactusKillItems", false);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Miscellaneous").push("misc");
        SERVER_BUILDER.pop();

        COMMON_BUILDER.comment("Mushroom Settings").push("mushroom");
        REPLACE_MUSHROOM_SAPLING_ON_PLACEMENT = COMMON_BUILDER.comment("Only relevant if 'replaceVanillaSapling' to be set to TRUE in Dynamic Trees common config. If enabled mushrooms will be replaced by their dynamic counterparts as soon as they are placed.").
                define("replaceMushroomSaplingOnPlacement", false);
        REPLACE_MUSHROOM_SAPLING_ON_GROWTH = COMMON_BUILDER.comment("Only relevant if 'replaceVanillaSapling' to be set to TRUE in Dynamic Trees common config. If enabled mushrooms will be replaced by their dynamic counterparts when they grow into a giant mushroom.").
                define("replaceMushroomSaplingOnGrowth", true);
        COMMON_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();
        COMMON_CONFIG = COMMON_BUILDER.build();
//        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

}
