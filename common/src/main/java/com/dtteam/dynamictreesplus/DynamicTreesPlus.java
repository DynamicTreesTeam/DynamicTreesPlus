package com.dtteam.dynamictreesplus;

import net.minecraft.resources.ResourceLocation;

public class DynamicTreesPlus {

    public static final String MOD_ID = "dynamictreesplus";
    public static final ResourceLocation CACTUS = DynamicTreesPlus.location("cactus");
    public static final ResourceLocation MUSHROOM = DynamicTreesPlus.location("mushroom");

    public static final ResourceLocation PILLAR = DynamicTreesPlus.location("pillar_cactus");
    public static final ResourceLocation PIPE = DynamicTreesPlus.location("pipe_cactus");
    public static final ResourceLocation SAGUARO = DynamicTreesPlus.location("saguaro_cactus");
    public static final ResourceLocation MEGA = DynamicTreesPlus.location("mega_cactus");

    public static ResourceLocation location(final String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

}
