package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.util.Registry;
import com.ferreusveritas.dynamictrees.util.RegistryEntry;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class CactusThicknessLogic extends RegistryEntry<CactusThicknessLogic> {

    public static final CactusThicknessLogic NULL_LOGIC = new CactusThicknessLogic(DynamicTreesPlus.resLoc("null")) {
        @Override public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) { return currentThickness; }
        @Override public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) { return CactusBranchBlock.CactusThickness.BRANCH; }
    };

    public static final Registry<CactusThicknessLogic> REGISTRY = new Registry<>(CactusThicknessLogic.class, NULL_LOGIC);

    public CactusThicknessLogic(ResourceLocation registryName) {
        super(registryName);
    }

    public abstract CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness);

    public abstract CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast);

}
