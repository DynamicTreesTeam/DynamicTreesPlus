package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.provider.BranchLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictreesplus.block.CactusBranchBlock;
import com.dtteam.dynamictreesplus.event.BakedModelEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.dtteam.dynamictrees.util.ResourceLocationUtils.suffix;

public class CactusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CactusFamily::new);

    public CactusFamily(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public MapColor getDefaultBranchMapColor() {
        return MapColor.PLANT;
    }

    @Override
    public SoundType getDefaultBranchSoundType() {
        return SoundType.WOOL;
    }

    @Override
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        return new CactusBranchBlock(name, this.getProperties());
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

    public static final String BRANCH_BOTTOM = "branch_bottom";

}
