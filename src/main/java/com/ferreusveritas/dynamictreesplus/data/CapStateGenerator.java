package com.ferreusveritas.dynamictreesplus.data;

import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapBlock;
import net.minecraft.world.level.block.Block;

public class CapStateGenerator implements Generator<DTBlockStateProvider, CapProperties> {

    public static final DependencyKey<DynamicCapBlock> CAP = new DependencyKey<>("cap");
    public static final DependencyKey<Block> PRIMITIVE_CAP = new DependencyKey<>("primitive_cap");

    @Override
    public void generate(DTBlockStateProvider provider, CapProperties input, Dependencies dependencies) {
        provider.simpleBlock(dependencies.get(CAP), provider.models().getExistingFile(
                provider.block(dependencies.get(PRIMITIVE_CAP).getRegistryName())
        ));
    }

    @Override
    public Dependencies gatherDependencies(CapProperties input) {
        return new Dependencies()
                .append(CAP, input.getDynamicCapBlock())
                .append(PRIMITIVE_CAP, input.getPrimitiveCapBlock());
    }

}
