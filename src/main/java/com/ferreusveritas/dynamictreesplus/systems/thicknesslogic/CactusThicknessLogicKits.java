package com.ferreusveritas.dynamictreesplus.systems.thicknesslogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.DynamicTreesPlus;
import com.ferreusveritas.dynamictreesplus.block.CactusBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CactusThicknessLogicKits {

    public static final CactusThicknessLogic PILLAR = new CactusThicknessLogic(DynamicTreesPlus.location("pillar")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            BlockState upState = world.getBlockState(pos.above());
            BlockState downState = world.getBlockState(pos.below());
            return (upState.getBlock() instanceof CactusBranchBlock && downState.getBlock() instanceof CactusBranchBlock) ? CactusBranchBlock.CactusThickness.TRUNK : CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.below());
            if (TreeHelper.isRooty(downState) || isLast)
                return CactusBranchBlock.CactusThickness.BRANCH;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }
    };

    public static final CactusThicknessLogic PIPE = new CactusThicknessLogic(DynamicTreesPlus.location("pipe")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast) {
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    };

    public static final CactusThicknessLogic SAGUARO = new CactusThicknessLogic(DynamicTreesPlus.location("saguaro")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            return currentThickness;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast) {
            BlockState downState = world.getBlockState(pos.below());
            if (TreeHelper.isRooty(downState) || (downState.getBlock() instanceof CactusBranchBlock && downState.getValue(CactusBranchBlock.TRUNK_TYPE) == CactusBranchBlock.CactusThickness.TRUNK && downState.getValue(CactusBranchBlock.ORIGIN) == Direction.DOWN))
                return CactusBranchBlock.CactusThickness.TRUNK;
            return CactusBranchBlock.CactusThickness.BRANCH;
        }
    };

    public static final CactusThicknessLogic MEGA = new CactusThicknessLogic(DynamicTreesPlus.location("mega")) {
        @Override
        public CactusBranchBlock.CactusThickness thicknessAfterGrowthSignal(Level world, BlockPos pos, GrowSignal signal, CactusBranchBlock.CactusThickness currentThickness) {
            Block down = world.getBlockState(pos.below()).getBlock();
            Block down2 = world.getBlockState(pos.below(2)).getBlock();
            Block down3 = world.getBlockState(pos.below(3)).getBlock();
            if (down instanceof RootyBlock || down2 instanceof RootyBlock || down3 instanceof RootyBlock)
                return CactusBranchBlock.CactusThickness.CORE;
            return CactusBranchBlock.CactusThickness.TRUNK;
        }

        @Override
        public CactusBranchBlock.CactusThickness thicknessForBranchPlaced(LevelAccessor world, BlockPos pos, boolean isLast) {
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
