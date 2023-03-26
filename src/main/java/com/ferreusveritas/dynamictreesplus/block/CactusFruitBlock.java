package com.ferreusveritas.dynamictreesplus.block;

import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusFruitBlock extends FruitBlock {


    public static final BooleanProperty OFFSET = BooleanProperty.create("is_offset");

    public CactusFruitBlock(Properties properties, Fruit fruit) {
        super(properties, fruit);
        registerDefaultState(defaultBlockState().setValue(OFFSET, false));
    }

    @Override
    public boolean isSupported(LevelReader world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.below()).getBlock() instanceof CactusBranchBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(OFFSET));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getShape(state, level, pos, context);
        if (state.getValue(OFFSET)) return shape.move(0,-0.25f,0);
        return shape;
    }
}
