package com.ferreusveritas.dynamictreesplus.model.geometry;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictreesplus.model.baked.CactusBranchBlockBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class CactusBranchBlockModelGeometry extends BranchBlockModelGeometry {

    public CactusBranchBlockModelGeometry(ResourceLocation barkTextureLocation, ResourceLocation ringsTextureLocation) {
        super(barkTextureLocation, ringsTextureLocation, null, false);
    }


    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new CactusBranchBlockBakedModel(modelLocation, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
    }

}
