package com.ferreusveritas.dynamictreesplus.tree;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BasicBranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.MushroomBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class HugeMushroomFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(HugeMushroomFamily::new);

    protected CapProperties commonCap = CapProperties.NULL;
    protected ResourceLocation insideBranchTexture;
    protected ResourceLocation outsideBranchTexture;

    public HugeMushroomFamily(ResourceLocation name) {
        super(name);
    }

    @Override
    public Family setPreReloadDefaults() {
        this.setPrimaryThickness(3);
        this.setSecondaryThickness(4);
        return this;
    }

    public CapProperties getCommonCap() {
        return this.commonCap;
    }

    public void setCommonCap(CapProperties properties) {
        this.commonCap = properties;
        properties.setFamily(this);
    }

    @Override
    public List<TagKey<Block>> defaultBranchTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Item>> defaultBranchItemTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Block>> defaultStrippedBranchTags() {
        return Collections.singletonList(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);
    }

    public void setInsideBranchTexture(ResourceLocation insideBranchTexture) {
        this.insideBranchTexture = insideBranchTexture;
    }

    public void setOutsideBranchTexture(ResourceLocation outsideBranchTexture) {
        this.outsideBranchTexture = outsideBranchTexture;
    }

    @Override
    public void addBranchTextures(BiConsumer<String, ResourceLocation> textureConsumer, ResourceLocation primitiveLogLocation) {
        textureConsumer.accept("bark", outsideBranchTexture);
        textureConsumer.accept("rings", insideBranchTexture);
    }

    public boolean isCompatibleCap (Species species, BlockState state, Level level, BlockPos pos){
        return true;
    }

    ///////////////////////////////////////////
    // CAP GROWTH
    ///////////////////////////////////////////


    @Override
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        final BasicBranchBlock branch = new MushroomBranchBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }
}
