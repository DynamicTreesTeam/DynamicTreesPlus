package com.ferreusveritas.dynamictreesplus.init;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod.EventBusSubscriber
public class DTPConfigs {

    public static File configDirectory;

    public static ForgeConfigSpec SERVER_CONFIG;
//    public static ForgeConfigSpec COMMON_CONFIG;
//    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.BooleanValue canBoneMealCactus;
    public static ForgeConfigSpec.BooleanValue cactusPrickleOnMoveOnly;
    public static ForgeConfigSpec.BooleanValue cactusKillItems;

    static {
        configDirectory = new File(FMLPaths.CONFIGDIR.get().toUri());

        final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
//        final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
//        final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("Cactus Settings").push("cactus");
        canBoneMealCactus = SERVER_BUILDER.comment("If enabled bone meal can be used to speed up cactus growth.").
                define("canBoneMealCactus", false);
        cactusPrickleOnMoveOnly = SERVER_BUILDER.comment("If enabled dynamic cacti will only prickle when moving (similar to how berry bushes prickle)").
                define("cactusPrickleOnMoveOnly", true);
        cactusKillItems = SERVER_BUILDER.comment("If enabled dynamic cacti will destroy items on contact").
                define("cactusKillItems", false);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Mushroom Settings").push("mushroom");
        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Miscellaneous").push("misc");
        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();
//        COMMON_CONFIG = COMMON_BUILDER.build();
//        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad (final ModConfig.Loading event) { }

}
