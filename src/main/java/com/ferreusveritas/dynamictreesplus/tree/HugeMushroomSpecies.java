package com.ferreusveritas.dynamictreesplus.tree;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.ferreusveritas.dynamictrees.util.Optionals;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapBlock;
import com.ferreusveritas.dynamictreesplus.block.mushroom.DynamicCapCenterBlock;
import com.ferreusveritas.dynamictreesplus.init.DTPRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.surround;

public class HugeMushroomSpecies extends Species {

    public static final Codec<Species> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString())
                            .forGetter(Species::getRegistryName),
                    Family.REGISTRY.getGetterCodec().fieldOf("family").forGetter(Species::getFamily),
                    CapProperties.REGISTRY.getGetterCodec().optionalFieldOf("cap_properties",
                            CapProperties.NULL).forGetter(species -> species instanceof HugeMushroomSpecies ? ((HugeMushroomSpecies)species).getCapProperties() : CapProperties.NULL))
            .apply(instance, HugeMushroomSpecies::new));

    public static final TypedRegistry.EntryType<Species> TYPE = TypedRegistry.newType(CODEC);

    protected CapProperties capProperties = CapProperties.NULL;
    protected boolean acceptAnySoil = true;
    protected int maxLightForPlanting = 12;

    public HugeMushroomSpecies(ResourceLocation name, Family family, CapProperties capProperties) {
        super(name, family, LeavesProperties.NULL);
        this.setCapProperties(capProperties.isValid() ? capProperties : (family instanceof HugeMushroomFamily ? ((HugeMushroomFamily)family).getCommonCap() : CapProperties.NULL));
    }

    @Override
    public Species setPreReloadDefaults() {
        this.setTransformable(false);
        return this.setSaplingShape(CommonVoxelShapes.ROUND_MUSHROOM)
                .setDefaultGrowingParameters()
                .envFactor(BiomeDictionary.Type.DRY, 0.25f)
                .envFactor(BiomeDictionary.Type.HOT, 0.75f)
                .envFactor(BiomeDictionary.Type.COLD, 1.05f)
                .setCanSaplingGrowNaturally(false)
                .setGrowthLogicKit(DTPRegistries.STRAIGHT_LOGIC);
    }

    public void setAcceptAnySoil(boolean acceptAnySoil) {
        this.acceptAnySoil = acceptAnySoil;
    }

    public void setMaxLightForPlanting(int maxLightForPlanting) {
        this.maxLightForPlanting = maxLightForPlanting;
    }

    public int getMaxLightForPlanting() {
        return maxLightForPlanting;
    }

    @Override
    protected boolean transitionToTree(Level level, BlockPos pos, Family family) {
        // Set to a single branch with 1 radius.
        family.getBranch().ifPresent(branch -> branch.setRadius(level, pos, family.getPrimaryThickness(), null));
        // Place a single leaf block on top.
        level.setBlockAndUpdate(pos.above(), getCapProperties().getDynamicCapState(true));
        // Set to fully fertilized rooty dirt underneath.
        placeRootyDirtBlock(level, pos.below(), 15);

        if (doesRequireTileEntity(level, pos)) {
            SpeciesBlockEntity speciesBlockEntity = DTRegistries.SPECIES_BLOCK_ENTITY.create(pos.below(), level.getBlockState(pos.below()));
            assert speciesBlockEntity != null;
            level.setBlockEntity(speciesBlockEntity);
            speciesBlockEntity.setSpecies(this);
        }

        return true;
    }

    ///////////////////////////////////////////
    // CAP
    ///////////////////////////////////////////

    public Species setCapProperties(CapProperties capProperties) {
        this.capProperties = capProperties;
        capProperties.setFamily(getFamily());
        return this;
    }

    public CapProperties getCapProperties() {
        return capProperties;
    }

    public Optional<DynamicCapCenterBlock> getCapCenterBlock() {
        return this.capProperties.getDynamicCapCenterBlock();
    }

    public Optional<DynamicCapBlock> getCapBlock() {
        return this.capProperties.getDynamicCapBlock();
    }

    public Optional<Block> getPrimitiveCap() {
        return Optionals.ofBlock(this.capProperties.getPrimitiveCap().getBlock());
    }

    @Override
    public List<TagKey<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<TagKey<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTrees.location("block/smartmodel/mushroom_" + (this.getSaplingShape() == CommonVoxelShapes.FLAT_MUSHROOM ? "flat" : "round"));
    }

    @Override
    public boolean isAcceptableSoil(LevelReader level, BlockPos pos, BlockState soilBlockState) {
        return (SoilHelper.isSoilAcceptable(soilBlockState, soilTypeFlags) || level.getRawBrightness(pos.above(), 0) <= getMaxLightForPlanting())
                && super.isAcceptableSoil(level, pos, soilBlockState);
    }

    @Override
    public boolean isAcceptableSoil(BlockState soilState) {
        if (acceptAnySoil){
            Block soilBlock = soilState.getBlock();
            return soilBlock instanceof RootyBlock || SoilHelper.isSoilRegistered(soilBlock);
        }
        return SoilHelper.isSoilAcceptable(soilState, soilTypeFlags);
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        final ResourceLocation capLocation = surround(this.getRegistryName(), "block/", "_cap");
        textureConsumer.accept("particle", capLocation);
        textureConsumer.accept("stem", capLocation);
        textureConsumer.accept("cap", capLocation);
    }

    ///////////////////////////////////////////
    // RENDER
    ///////////////////////////////////////////

    @Nullable
    public HashMap<BlockPos, BlockState> getFellingLeavesClusters(final BranchDestructionData destructionData) {
        HashMap<BlockPos, BlockState> map = new HashMap<>();

        for (int pos : destructionData.destroyedLeaves){

        }

        return map;
    }

}
