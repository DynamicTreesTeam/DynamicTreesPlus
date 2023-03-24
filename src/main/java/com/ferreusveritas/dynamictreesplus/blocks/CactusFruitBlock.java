package com.ferreusveritas.dynamictreesplus.blocks;

import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class CactusFruitBlock extends FruitBlock {


    public static final BooleanProperty OFFSET = BooleanProperty.create("is_offset");

    public CactusFruitBlock(Properties properties, Fruit fruit) {
        super(properties, fruit);
        registerDefaultState(defaultBlockState().setValue(OFFSET, false));
    }

    @Override
    public boolean isSupported(IBlockReader world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.below()).getBlock() instanceof CactusBranchBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(OFFSET));
    }

}
