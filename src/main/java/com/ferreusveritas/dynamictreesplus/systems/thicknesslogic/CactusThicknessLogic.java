package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.CactusBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;


public abstract class CactusThicknessLogic extends RegistryEntry<CactusThicknessLogic> {

    public static final CactusThicknessLogic NULL_LOGIC = new CactusThicknessLogic(DynamicTreesPlus.location("null")) {
        @Override public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) { return currentThickness; }
        @Override public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast) { return CactusBranchBlock.CactusThickness.BRANCH; }
    };

    public static final Registry<CactusThicknessLogic> REGISTRY = new SimpleRegistry<>(CactusThicknessLogic.class, NULL_LOGIC);

    public CactusThicknessLogic(ResourceLocation registryName) {
        super(registryName);
    }

    public abstract CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness);

    public abstract CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast);

}
