package com.dtteam.dynamictreesplus.init;

import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.registries.ForgeRegistries;

public class DTPClient {

    public static void setup() {
        registerRenderLayers();
    }

    private static void registerRenderLayers () {
        ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof CactusBranchBlock)
                .forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));
    }

}
