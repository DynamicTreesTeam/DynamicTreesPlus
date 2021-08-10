package com.ferreusveritas.dynamictreesplus.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.ferreusveritas.dynamictreesplus.event.BakedModelEventHandler;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.suffix;

public class CactusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CactusFamily::new);

    public CactusFamily(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public Material getDefaultBranchMaterial() {
        return Material.CACTUS;
    }

    @Override
    public SoundType getDefaultBranchSoundType() {
        return SoundType.WOOL;
    }

    @Override
    protected BranchBlock createBranchBlock() {
        return new CactusBranchBlock(this.getProperties());
    }

    @Override
    public int getPrimaryThickness() {
        return 5;
    }

    @Override
    public int getSecondaryThickness() {
        return 4;
    }

    @Override
    public BiFunction<BlockModelBuilder, ExistingFileHelper, BranchLoaderBuilder> getBranchLoaderConstructor() {
        return (parent, existingFileHelper) -> new BranchLoaderBuilder(BakedModelEventHandler.CACTUS, parent, existingFileHelper);
    }

    @Override
    public void addBranchTextures(BiConsumer<String, ResourceLocation> textureConsumer, ResourceLocation primitiveLogLocation) {
        textureConsumer.accept("bark", suffix(primitiveLogLocation, "_side"));
        textureConsumer.accept("rings", suffix(primitiveLogLocation, "_top"));
    }

}
