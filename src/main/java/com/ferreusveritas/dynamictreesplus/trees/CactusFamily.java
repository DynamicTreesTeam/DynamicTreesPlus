package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class CactusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CactusFamily::new);

    public CactusFamily(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public LeavesProperties getCommonLeaves() {
        return Objects.requireNonNull(LeavesProperties.REGISTRY.get(DynamicTreesPlus.resLoc("bare")));
    }

    @Override
    protected BranchBlock createBranchBlock() {
        return new CactusBranchBlock();
    }

    @Override
    public int getPrimaryThickness() {
        return 5;
    }

    @Override
    public int getSecondaryThickness() {
        return 4;
    }

}
