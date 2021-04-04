package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public final class CactusThicknessLogicKits {

    public static final CactusThicknessLogic PILLAR = new CactusThicknessLogic(DynamicTreesPlus.resLoc("pillar")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            BlockState upState = world.getBlockState(pos.above());
            BlockState downState = world.getBlockState(pos.below());
            return (upState.getBlock() instanceof CactusBranchBlock && downState.getBlock() instanceof CactusBranchBlock) ? CactusBranchBlock.CactusThickness.TRUNK : CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.below());
            if (TreeHelper.isRooty(downState) || isLast)
                return CactusBranchBlock.CactusThickness.BRANCH;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }
    };

    public static final CactusThicknessLogic PIPE = new CactusThicknessLogic(DynamicTreesPlus.resLoc("pipe")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    };

    public static final CactusThicknessLogic SAGUARO = new CactusThicknessLogic(DynamicTreesPlus.resLoc("saguaro")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return currentThickness;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.below());
            if (TreeHelper.isRooty(downState) || (downState.getBlock() instanceof CactusBranchBlock && downState.getValue(CactusBranchBlock.TRUNK_TYPE) == CactusBranchBlock.CactusThickness.TRUNK && downState.getValue(CactusBranchBlock.ORIGIN) == Direction.DOWN))
                return CactusBranchBlock.CactusThickness.TRUNK;
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    };

    public static final CactusThicknessLogic MEGA = new CactusThicknessLogic(DynamicTreesPlus.resLoc("mega")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(World world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            Block down = world.getBlockState(pos.below()).getBlock();
            Block down2 = world.getBlockState(pos.below(2)).getBlock();
            Block down3 = world.getBlockState(pos.below(3)).getBlock();
            if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
                return CactusBranchBlock.CactusThickness.CORE;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(IWorld world, BlockPos pos, boolean isLast) {
            Block down = world.getBlockState(pos.below()).getBlock();
            Block down2 = world.getBlockState(pos.below(2)).getBlock();
            Block down3 = world.getBlockState(pos.below(3)).getBlock();

            if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
                return CactusBranchBlock.CactusThickness.CORE;
            if (down instanceof CactusBranchBlock)
                return CactusBranchBlock.CactusThickness.TRUNK;
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    };

}
