package com.ferreusveritas.dynamictreesplus.block.mushroom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface UpdatesSurroundNeighbors {

    Vec3i[] cornersAndEdges = {
            new Vec3i(1,1,1), new Vec3i(-1,1,1), new Vec3i(1,1,-1), new Vec3i(-1,1,-1),
            new Vec3i(1,-1,1), new Vec3i(-1,-1,1), new Vec3i(1,-1,-1), new Vec3i(-1,-1,-1),
            new Vec3i(0,1,1), new Vec3i(0,-1,1), new Vec3i(0,1,-1),new Vec3i(0,-1,-1),
            new Vec3i(1,0,1), new Vec3i(-1,0,1), new Vec3i(1,0,-1), new Vec3i(-1,0,-1),
            new Vec3i(1,1,0), new Vec3i(-1,1,0), new Vec3i(1,-1,0), new Vec3i(-1,-1,0),
    };
    default void updateNeighborsSurround (Level level, BlockPos pos, Class<? extends Block> blockClass){
        for (Vec3i corner : cornersAndEdges){
            BlockPos offPos = pos.offset(corner);
            Block offBlock = level.getBlockState(offPos).getBlock();
            if (blockClass.isInstance(offBlock))
                level.neighborChanged(offPos, offBlock, pos);
        }
    }

}
