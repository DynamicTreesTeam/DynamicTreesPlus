//package com.dtteam.dynamictreesplus.model.loader;
//
//import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
//import com.dtteam.dynamictrees.model.loader.BranchBlockModelLoader;
//import com.dtteam.dynamictreesplus.model.geometry.CactusBranchBlockModelGeometry;
//import net.minecraft.resources.Identifier;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//
//import javax.annotation.Nullable;
//
///**
// * @author Harley O'Connor
// */
//@OnlyIn(Dist.CLIENT)
//public class CactusBlockModelLoader extends BranchBlockModelLoader {
//
//    @Override
//    protected BranchBlockModelGeometry getModelGeometry(final Identifier barkResLoc, final Identifier ringsResLoc, @Nullable final Identifier familyResLoc) {
//        return new CactusBranchBlockModelGeometry(barkResLoc, ringsResLoc);
//    }
//
//}
