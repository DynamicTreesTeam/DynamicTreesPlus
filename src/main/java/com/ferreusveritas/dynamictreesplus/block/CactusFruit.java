package com.ferreusveritas.dynamictreesplus.block;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CactusFruit extends Fruit {

    public static final TypedRegistry.EntryType<Fruit> TYPE = TypedRegistry.newType(CactusFruit::new);

    public CactusFruit(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected FruitBlock createBlock(Block.Properties properties) {
        return new CactusFruitBlock(properties, this);
    }

    @Override
    public void place(LevelAccessor world, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(0);
        state = offsetBlockIfOnTop(world, pos, state);
        world.setBlock(pos, state, Block.UPDATE_ALL);
    }

    @Override
    public void placeDuringWorldGen(LevelAccessor world, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(getAgeForWorldGen(world, pos, seasonValue));
        state = offsetBlockIfOnTop(world, pos, state);
        world.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private BlockState offsetBlockIfOnTop(LevelAccessor world, BlockPos pos, BlockState inState){
        BlockState downState = world.getBlockState(pos.below());
        BranchBlock downBranch = TreeHelper.getBranch(world.getBlockState(pos.below()));
        if (downBranch instanceof CactusBranchBlock){
            if (downState.getValue(CactusBranchBlock.ORIGIN) == Direction.DOWN)
                return inState.setValue(CactusFruitBlock.OFFSET, true);
        }
        return inState;
    }

}
