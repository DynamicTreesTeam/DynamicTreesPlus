package com.dtteam.dynamictreesplus.data;

import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.block.mushroom.DynamicCapBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class CapStateGenerator implements Generator<DTDataProvider.BlockState, CapProperties> {

    public static final DependencyKey<DynamicCapBlock> CAP = new DependencyKey<>("cap");
    public static final DependencyKey<Block> PRIMITIVE_CAP = new DependencyKey<>("primitive_cap");

    @Override
    public void generate(DTDataProvider.BlockState prov, CapProperties input, Dependencies dependencies) {
        if (prov instanceof DTBlockStateProvider provider){
            Identifier outLocation = provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_CAP)));
            Identifier inLocation = Identifier.parse("block/mushroom_block_inside");
            ModelFile outFaceModel;
            ModelFile inFaceModel;
            if (input.shouldGenerateFaceModels()){
                final BlockModelBuilder outFaceBuilder = provider.models().getBuilder(input.getCapFaceModelName())
                        .parent(provider.models().getExistingFile(input.getFaceModelParent()));
                input.addCapFaceTextures(outFaceBuilder::texture, outLocation, false);
                outFaceModel = outFaceBuilder;
                final BlockModelBuilder inFaceBuilder = provider.models().getBuilder(input.getCapInsideFaceModelName())
                        .parent(provider.models().getExistingFile(input.getFaceModelParent()));
                input.addCapFaceTextures(inFaceBuilder::texture, inLocation, true);
                inFaceModel = inFaceBuilder;
            } else {
                outFaceModel = provider.models().getExistingFile(
                        input.getModelPath(CapProperties.OUTSIDE_FACE).orElse(outLocation)
                );
                inFaceModel = provider.models().getExistingFile(
                        input.getModelPath(CapProperties.INSIDE_FACE).orElse(inLocation)
                );
            }

            provider.getMultipartBuilder(dependencies.get(CAP))
                    .part().modelFile(outFaceModel).uvLock(true)
                    .addModel().condition(DynamicCapBlock.NORTH, true)
                    .end()
                    .part().modelFile(inFaceModel)
                    .addModel().condition(DynamicCapBlock.NORTH, false)
                    .end()

                    .part().modelFile(outFaceModel).rotationY(90).uvLock(true)
                    .addModel().condition(DynamicCapBlock.EAST, true)
                    .end()
                    .part().modelFile(inFaceModel).rotationY(90)
                    .addModel().condition(DynamicCapBlock.EAST, false)
                    .end()

                    .part().modelFile(outFaceModel).rotationY(180).uvLock(true)
                    .addModel().condition(DynamicCapBlock.SOUTH, true)
                    .end()
                    .part().modelFile(inFaceModel).rotationY(180)
                    .addModel().condition(DynamicCapBlock.SOUTH, false)
                    .end()

                    .part().modelFile(outFaceModel).rotationY(270).uvLock(true)
                    .addModel().condition(DynamicCapBlock.WEST, true)
                    .end()
                    .part().modelFile(inFaceModel).rotationY(270)
                    .addModel().condition(DynamicCapBlock.WEST, false)
                    .end()

                    .part().modelFile(outFaceModel).rotationX(270).uvLock(true)
                    .addModel().condition(DynamicCapBlock.UP, true)
                    .end()
                    .part().modelFile(inFaceModel).rotationX(270)
                    .addModel().condition(DynamicCapBlock.UP, false)
                    .end()

                    .part().modelFile(outFaceModel).rotationX(90).uvLock(true)
                    .addModel().condition(DynamicCapBlock.DOWN, true)
                    .end()
                    .part().modelFile(inFaceModel).rotationX(90)
                    .addModel().condition(DynamicCapBlock.DOWN, false)
                    .end();
        }
    }

    @Override
    public Dependencies gatherDependencies(CapProperties input) {
        return new Dependencies()
                .append(CAP, input.getDynamicCapBlock())
                .append(PRIMITIVE_CAP, input.getPrimitiveCapBlock());
    }

}
