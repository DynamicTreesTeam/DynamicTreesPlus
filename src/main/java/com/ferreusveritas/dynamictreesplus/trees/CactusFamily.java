package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class CactusFamily extends Family {

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
    public float getPrimaryThickness() {
        return 5.0f;
    }

    @Override
    public float getSecondaryThickness() {
        return 4.0f;
    }

    public static final class Type extends Family.Type {
        @Override
        public CactusFamily construct(ResourceLocation resourceLocation) {
            return new CactusFamily(resourceLocation);
        }
    }

}
