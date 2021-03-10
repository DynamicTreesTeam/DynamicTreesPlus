package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public abstract class CactusThicknessLogic {

    private static final Map<ResourceLocation, CactusThicknessLogic> REGISTRY = new HashMap<>();

    public static void register(final ResourceLocation registryName, final CactusThicknessLogic cactusThicknessLogic) {
        REGISTRY.put(registryName, cactusThicknessLogic);
    }

    public static CactusThicknessLogic get(final ResourceLocation registryName) {
        return REGISTRY.get(registryName);
    }

    public abstract CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness);

    public abstract CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast);

}
