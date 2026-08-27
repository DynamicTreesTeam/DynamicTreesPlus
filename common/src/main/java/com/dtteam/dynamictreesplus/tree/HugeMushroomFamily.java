package com.dtteam.dynamictreesplus.tree;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.block.mushroom.MushroomBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class HugeMushroomFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(HugeMushroomFamily::new);

    protected CapProperties commonCap = CapProperties.NULL;

    public HugeMushroomFamily(Identifier name) {
        super(name);
    }

    @Override
    public Family setPreReloadDefaults() {
        this.setPrimaryThickness(2);
        this.setSecondaryThickness(3);
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

    @Override
    public BlockPosBounds expandLeavesBlockBounds(BlockPosBounds bounds) {
        return bounds.expand(8);
    }

    ///////////////////////////////////////////
    // CAP GROWTH
    ///////////////////////////////////////////

    public boolean isCompatibleCap (HugeMushroomSpecies species, BlockState state, Level level, BlockPos pos){
        return species.getCapProperties().isPartOfCap(state);
    }

    @Override
    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        final BasicBranchBlock branch = new MushroomBranchBlock(name, properties);
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }

}
