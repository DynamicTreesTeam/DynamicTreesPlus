package com.dtteam.dynamictreesplus.model.loader;

import com.dtteam.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.models.loader.BranchBlockModelLoader;
import com.dtteam.dynamictreesplus.model.geometry.CactusBranchBlockModelGeometry;
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
