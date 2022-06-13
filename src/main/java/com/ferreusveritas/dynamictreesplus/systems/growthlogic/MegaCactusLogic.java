package com.ferreusveritas.dynamictreesplus.systems.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class MegaCactusLogic extends CactusLogic {

    private static final ConfigurationProperty<Integer> STOP_BRANCHING_HEIGHT = ConfigurationProperty.integer("stop_branching_height");
    private static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");

    public MegaCactusLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        super.registerProperties();
        this.register(STOP_BRANCHING_HEIGHT, MAX_HEIGHT);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(STOP_BRANCHING_HEIGHT, 5)
                .with(MAX_HEIGHT, 7);
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = context.probMap();
        final GrowSignal signal = context.signal();
        final Level world = context.world();
        final BlockPos pos = context.pos();
        final Direction originDir = signal.dir.getOpposite();

        int height = pos.getY() - signal.rootPos.getY();

        if (height >= configuration.get(MAX_HEIGHT) && world.random.nextFloat() < 0.8f){
            signal.energy = 0;
            return new int[]{0,0,0,0,0,0};
        }

        if (height > configuration.get(STOP_BRANCHING_HEIGHT)){
            //When above a certain height, all branches should grow straight up
            return new int[]{0,1,0,0,0,0};
        }

        //Alter probability map for direction change
        probMap[0] = 0; //Down is always disallowed for cactus
        probMap[1] = (int) (context.species().getUpProbability() + signal.rootPos.distSqr(new Vec3i(pos.getX(), signal.rootPos.getY(), pos.getZ())) * 0.8);
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = world.getBlockState(pos.above()).getBlock() instanceof CactusBranchBlock && signal.energy > 1 ? 3 : 0;
        if (signal.dir != Direction.UP) probMap[signal.dir.ordinal()] = 0; //Disable the current direction, unless that direction is up
        probMap[originDir.ordinal()] = 0; //Disable the direction we came from

        return probMap;
    }

}
