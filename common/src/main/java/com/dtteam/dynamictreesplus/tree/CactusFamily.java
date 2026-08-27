package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CactusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CactusFamily::new);

    public CactusFamily(final Identifier registryName) {
        super(registryName);
    }

    @Override
    public BlockBehaviour.Properties defaultBranchProperties() {
        return super.defaultBranchProperties()
                .sound(SoundType.WOOL)
                .mapColor(MapColor.PLANT);
    }

    @Override
    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new CactusBranchBlock(name, properties);
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
    public Identifier getBranchLoader() {
        return DynamicTreesPlus.CACTUS;
    }

    public static final String BRANCH_BOTTOM = "branch_bottom";

}
