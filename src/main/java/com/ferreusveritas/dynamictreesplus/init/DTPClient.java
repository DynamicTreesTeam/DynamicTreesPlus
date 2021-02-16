package com.ferreusveritas.dynamictreesplus.init;

import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.registries.ForgeRegistries;

public class DTPClient {

    public static void setup() {
        registerRenderLayers();
    }

    private static void registerRenderLayers () {
        ForgeRegistries.BLOCKS.forEach(block -> {
            if (block instanceof CactusBranchBlock) {
                RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
            }
        });
    }

}
