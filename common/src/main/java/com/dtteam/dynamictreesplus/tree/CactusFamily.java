package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CactusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CactusFamily::new);

    public CactusFamily(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public BlockBehaviour.Properties getDefaultBranchProperties() {
        return super.getDefaultBranchProperties()
                .sound(SoundType.WOOL)
                .mapColor(MapColor.PLANT);
    }

    @Override
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        return new CactusBranchBlock(name, this.getProperties());
    }

    @Override
    public int getPrimaryThickness() {
        return 5;
    }

    @Override
    public int getSecondaryThickness() {
        return 4;
    }

    @Override
    public ResourceLocation getBranchLoader() {
        return DynamicTreesPlus.CACTUS;
    }

    public static final String BRANCH_BOTTOM = "branch_bottom";

}
