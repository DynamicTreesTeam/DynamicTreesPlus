package com.ferreusveritas.dynamictreesplus.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class CactusFruit extends Fruit {

    public static final TypedRegistry.EntryType<Fruit> TYPE = TypedRegistry.newType(CactusFruit::new);

    public CactusFruit(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected FruitBlock createBlock(AbstractBlock.Properties properties) {
        return new CactusFruitBlock(properties, this);
    }

    public void place(IWorld world, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(0);
        state = offsetBlockIfOnTop(world, pos, state);
        world.setBlock(pos, state, Constants.BlockFlags.DEFAULT);
    }

    public void placeDuringWorldGen(IWorld world, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(getAgeForWorldGen(world, pos, seasonValue));
        state = offsetBlockIfOnTop(world, pos, state);
        world.setBlock(pos, state, Constants.BlockFlags.DEFAULT);
    }

    private BlockState offsetBlockIfOnTop(IWorld world, BlockPos pos, BlockState inState){
        BlockState downState = world.getBlockState(pos.below());
        BranchBlock downBranch = TreeHelper.getBranch(world.getBlockState(pos.below()));
        if (downBranch instanceof CactusBranchBlock){
            if (downState.getValue(CactusBranchBlock.ORIGIN) == Direction.DOWN)
                return inState.setValue(CactusFruitBlock.OFFSET, true);
        }
        return inState;
    }

}
