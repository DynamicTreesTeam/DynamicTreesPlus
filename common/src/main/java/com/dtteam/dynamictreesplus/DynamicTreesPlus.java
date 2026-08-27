package com.dtteam.dynamictreesplus;

import net.minecraft.resources.Identifier;

public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";
    public static final Identifier CACTUS = DynamicTreesPlus.location("cactus");
    public static final Identifier MUSHROOM = DynamicTreesPlus.location("mushroom");

    public static final Identifier PILLAR = DynamicTreesPlus.location("pillar_cactus");
    public static final Identifier PIPE = DynamicTreesPlus.location("pipe_cactus");
    public static final Identifier SAGUARO = DynamicTreesPlus.location("saguaro_cactus");
    public static final Identifier MEGA = DynamicTreesPlus.location("mega_cactus");

    public static Identifier location(final String path) {
        return Identifier.tryBuild(MOD_ID, path);
    }

}
