package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class CactusThicknessLogicKits {

    public static final CactusThicknessLogic PILLAR = new PillarThicknessLogic();
    public static final CactusThicknessLogic PIPE = new PipeThicknessLogic();
    public static final CactusThicknessLogic SAGUARO = new SaguaroThicknessLogic();
    public static final CactusThicknessLogic MEGA = new MegaThicknessLogic();

    public static void register () {
        register(DynamicTreesPlus.resLoc("pillar"), PILLAR);
        register(DynamicTreesPlus.resLoc("pipe"), PIPE);
        register(DynamicTreesPlus.resLoc("saguaro"), SAGUARO);
        register(DynamicTreesPlus.resLoc("mega"), MEGA);
    }

    private static void register (final ResourceLocation registryName, final CactusThicknessLogic cactusThicknessLogic) {
        CactusThicknessLogic.register(registryName, cactusThicknessLogic);
    }

    private static class PillarThicknessLogic extends CactusThicknessLogic {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            BlockState upState = world.getBlockState(pos.up());
            BlockState downState = world.getBlockState(pos.down());
            return (upState.getBlock() instanceof CactusBranchBlock && downState.getBlock() instanceof CactusBranchBlock) ? CactusBranchBlock.CactusThickness.TRUNK : CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.down());
            if (TreeHelper.isRooty(downState) || isLast)
                return CactusBranchBlock.CactusThickness.BRANCH;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }
    }

    private static class PipeThicknessLogic extends CactusThicknessLogic {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    }

    private static class SaguaroThicknessLogic extends CactusThicknessLogic {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return currentThickness;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.down());
            if (TreeHelper.isRooty(downState) || (downState.getBlock() instanceof CactusBranchBlock && downState.get(CactusBranchBlock.TRUNK_TYPE) == CactusBranchBlock.CactusThickness.TRUNK && downState.get(CactusBranchBlock.ORIGIN) == Direction.DOWN))
                return CactusBranchBlock.CactusThickness.TRUNK;
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    }

    private static class MegaThicknessLogic extends CactusThicknessLogic {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            Block down = world.getBlockState(pos.down()).getBlock();
            Block down2 = world.getBlockState(pos.down(2)).getBlock();
            Block down3 = world.getBlockState(pos.down(3)).getBlock();
            if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
                return CactusBranchBlock.CactusThickness.CORE;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            Block down = world.getBlockState(pos.down()).getBlock();
            Block down2 = world.getBlockState(pos.down(2)).getBlock();
            Block down3 = world.getBlockState(pos.down(3)).getBlock();

            if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
                return CactusBranchBlock.CactusThickness.CORE;
            if (down instanceof CactusBranchBlock)
                return CactusBranchBlock.CactusThickness.TRUNK;
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    }

}
