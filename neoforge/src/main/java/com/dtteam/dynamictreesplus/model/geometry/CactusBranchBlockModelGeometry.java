//package com.dtteam.dynamictreesplus.model.geometry;
//
//import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
//import com.dtteam.dynamictreesplus.model.baked.CactusBranchBlockBakedModel;
//import net.minecraft.client.renderer.block.model.ItemOverrides;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.resources.model.BakedModel;
//import net.minecraft.client.resources.model.Material;
//import net.minecraft.client.resources.model.ModelBaker;
//import net.minecraft.client.resources.model.ModelState;
//import net.minecraft.resources.Identifier;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
//
//import java.util.function.Function;
//
///**
// * @author Harley O'Connor
// */
//@OnlyIn(Dist.CLIENT)
//public class CactusBranchBlockModelGeometry extends BranchBlockModelGeometry {
//
//    public CactusBranchBlockModelGeometry(Identifier barkTextureLocation, Identifier ringsTextureLocation) {
//        super(barkTextureLocation, ringsTextureLocation, null, false);
//    }
//
//    @Override
//    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
//        return new CactusBranchBlockBakedModel(this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
//    }
//
//}
