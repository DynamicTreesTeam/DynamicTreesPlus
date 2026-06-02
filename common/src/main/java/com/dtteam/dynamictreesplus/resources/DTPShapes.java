package com.dtteam.dynamictreesplus.resources;

import com.dtteam.dynamictrees.block.CommonVoxelShapes;
import com.dtteam.dynamictreesplus.DynamicTreesPlus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DTPShapes {

    public static VoxelShape TALL_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.6875f, 0.625f));
    public static VoxelShape MEDIUM_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.5625f, 0.625f));
    public static VoxelShape SHORT_CACTUS_SAPLING_SHAPE = Shapes.create(new AABB(0.375f, 0.0f, 0.375f, 0.625f, 0.4375f, 0.625f));

    public static void setup() {
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("tall_cactus").toString(), TALL_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("medium_cactus").toString(), MEDIUM_CACTUS_SAPLING_SHAPE);
        CommonVoxelShapes.SHAPES.put(DynamicTreesPlus.location("short_cactus").toString(), SHORT_CACTUS_SAPLING_SHAPE);

    }

}
