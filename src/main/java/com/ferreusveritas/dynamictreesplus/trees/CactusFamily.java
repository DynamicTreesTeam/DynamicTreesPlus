package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.FamilyType;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class CactusFamily extends Family {

    public static final FamilyType<CactusFamily> CACTUS_FAMILY = new Type();

    public CactusFamily(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public LeavesProperties getCommonLeaves() {
        return Objects.requireNonNull(LeavesProperties.REGISTRY.getValue(DynamicTreesPlus.resLoc("bare")));
    }

    @Override
    public BranchBlock createBranch() {
        final ResourceLocation branchRegName = new ResourceLocation(this.getRegistryName().getNamespace(), this.getRegistryName().getPath() + "_branch");
        return new CactusBranchBlock(branchRegName);
    }

    @Override
    public float getPrimaryThickness() {
        return 5.0f;
    }

    @Override
    public float getSecondaryThickness() {
        return 4.0f;
    }

    public static final class Type extends FamilyType<CactusFamily> {
        private Type() {
            super(DynamicTreesPlus.resLoc("cactus"));
        }

        @Override
        public CactusFamily construct(ResourceLocation resourceLocation) {
            return new CactusFamily(resourceLocation);
        }
    }

}
