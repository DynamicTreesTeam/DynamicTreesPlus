package com.ferreusveritas.dynamictreesplus.model.loader;

import com.ferreusveritas.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.model.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictreesplus.model.geometry.CactusBranchBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class CactusBlockModelLoader extends BranchBlockModelLoader {

    @Override
    protected BranchBlockModelGeometry getModelGeometry(final ResourceLocation barkResLoc, final ResourceLocation ringsResLoc, @Nullable final ResourceLocation familyResLoc) {
        return new CactusBranchBlockModelGeometry(barkResLoc, ringsResLoc);
    }

}
