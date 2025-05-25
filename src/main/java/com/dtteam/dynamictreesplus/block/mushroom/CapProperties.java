package com.dtteam.dynamictreesplus.block.mushroom;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.lazyvalue.MutableLazyValue;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.loot.DTLootParameterSets;
import com.dtteam.dynamictrees.loot.LootTableSupplier;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.utility.Optionals;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.dtteam.dynamictreesplus.data.CapCenterStateGenerator;
import com.dtteam.dynamictreesplus.data.CapStateGenerator;
import com.dtteam.dynamictreesplus.data.DTPLootTableHandler;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomCapDisc;
import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class CapProperties extends RegistryEntry<CapProperties> implements Resettable<CapProperties> {

    public static final Codec<CapProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(TypedRegistry.RESOURCE_LOCATION.toString()).forGetter(CapProperties::getRegistryName))
            .apply(instance, CapProperties::new));

    public static final CapProperties NULL = new CapProperties() {
        @Override
        public Family getFamily() {
            return Family.NULL_FAMILY;
        }

        @Override
        public BlockState getPrimitiveCap() {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public ItemStack getPrimitiveCapItemStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public CapProperties setDynamicCapState(BlockState state, boolean center) {
            return this;
        }

        @Override
        public BlockState getDynamicCapState(boolean center) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public BlockState getDynamicCapState(int distance) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public int getFlammability() {
            return 0;
        }

        @Override
        public int getFireSpreadSpeed() {
            return 0;
        }
    }.setRegistryName(DynamicTrees.NULL).setBlockRegistryName(DynamicTrees.NULL);

    /**
     * Central registry for all {@link CapProperties} objects.
     * <p>
     * TO-DO: make it work with the RegistryCommand
     */
    public static final TypedRegistry<CapProperties> REGISTRY = new TypedRegistry<>(CapProperties.class, NULL, new TypedRegistry.EntryType<>(CODEC));

    private CapProperties() {
        this.blockLootTableSupplier = new LootTableSupplier("null/", DynamicTrees.NULL);
        this.lootTableSupplier = new LootTableSupplier("null/", DynamicTrees.NULL);
    }

    public CapProperties(final ResourceLocation registryName) {
        this(null, registryName);
    }

    public CapProperties(@Nullable final BlockState primitiveCap, final ResourceLocation registryName) {
        this.family = Family.NULL_FAMILY;
        this.primitiveCap = primitiveCap != null ? primitiveCap : Blocks.AIR.defaultBlockState();
        this.setRegistryName(registryName);
        this.centerBlockRegistryName = ResourceLocationUtils.suffix(registryName, this.getCenterBlockRegistryNameSuffix());
        this.blockRegistryName = ResourceLocationUtils.suffix(registryName, this.getBlockRegistryNameSuffix());
        this.blockLootTableSupplier = new LootTableSupplier("blocks/", blockRegistryName);
        this.lootTableSupplier = new LootTableSupplier("trees/mushroom_caps/", registryName);
    }

    /**
     * The primitive (vanilla) mushroom block is used for many purposes including rendering, drops, and some other basic
     * behavior.
     */
    protected BlockState primitiveCap;
    protected Family family;
    protected BlockState[] dynamicMushroomBlockDistanceStates = new BlockState[MushroomCapDisc.MAX_RADIUS + 1];
    protected BlockState dynamicMushroomCenterBlock;
    protected int flammability = 0;// Mimic vanilla mushroom
    protected int fireSpreadSpeed = 0;// Mimic vanilla mushroom
    protected VoxelShape ageZeroShape = Shapes.block();

    ///////////////////////////////////////////
    // PROPERTIES
    ///////////////////////////////////////////

    /**
     * Gets the primitive (vanilla) mushroom block for these {@link CapProperties}.
     *
     * @return The {@link BlockState} for the primitive mushroom block.
     */
    public BlockState getPrimitiveCap() {
        return primitiveCap;
    }

    public Optional<Block> getPrimitiveCapBlock() {
        return Optionals.ofBlock(this.primitiveCap == null ? null : this.primitiveCap.getBlock());
    }

    public void setPrimitiveCap(final Block primitiveCap) {
        if (this.primitiveCap == null || primitiveCap != this.primitiveCap.getBlock()) {
            this.primitiveCap = primitiveCap.defaultBlockState();
        }
    }

    /**
     * Gets {@link ItemStack} of the primitive (vanilla) mushroom block (for things like when it's silk-touched).
     *
     * @return The {@link ItemStack} object.
     */
    public ItemStack getPrimitiveCapItemStack() {
        return new ItemStack(Item.BY_BLOCK.get(getPrimitiveCap().getBlock()));
    }

    public Family getFamily() {
        return family;
    }

    public CapProperties setFamily(Family family) {
        this.family = family;
        if (family.isFireProof()) {
            flammability = 0;
            fireSpreadSpeed = 0;
        }
        return this;
    }

    public int getFlammability() {
        return flammability;
    }

    public void setFlammability(int flammability) {
        this.flammability = flammability;
    }

    public int getFireSpreadSpeed() {
        return fireSpreadSpeed;
    }

    public void setFireSpreadSpeed(int fireSpreadSpeed) {
        this.fireSpreadSpeed = fireSpreadSpeed;
    }

    public MapColor getDefaultMapColor() {
        return MapColor.WOOD;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(final MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(0.2F)
                .sound(SoundType.WOOD);
    }

    public int getMaxAge(HugeMushroomSpecies species) {
        return species.getMushroomShapeKit().getMaxCapAge();
    }

    public void setAgeZeroShape(VoxelShape ageZeroShape) {
        this.ageZeroShape = ageZeroShape;
    }

    public VoxelShape getAgeZeroShape() {
        return ageZeroShape;
    }

    ///////////////////////////////////////////
    // DYNAMIC CAP BLOCK
    ///////////////////////////////////////////

    /**
     * The registry name for the leaves block. This allows for built-in compatibility where the dynamic leaves may
     * otherwise share the same name as their regular leaves block.
     */
    private ResourceLocation blockRegistryName;
    private ResourceLocation centerBlockRegistryName;

    /**
     * Gets the {@link #blockRegistryName} for this {@link CapProperties} object.
     *
     * @return The {@link #blockRegistryName} for this {@link CapProperties} object.
     */
    public ResourceLocation getBlockRegistryName() {
        return this.blockRegistryName;
    }

    public ResourceLocation getCenterBlockRegistryName() {
        return this.centerBlockRegistryName;
    }

    /**
     * Sets the {@link #blockRegistryName} for this {@link CapProperties} object to the specified {@code
     * blockRegistryName}.
     *
     * @param blockRegistryName The new {@link ResourceLocation} object to set.
     * @return This {@link CapProperties} object for chaining.
     */
    public CapProperties setBlockRegistryName(ResourceLocation blockRegistryName) {
        this.blockRegistryName = blockRegistryName;
        return this;
    }

    public CapProperties setCenterBlockRegistryName(ResourceLocation blockRegistryName) {
        this.centerBlockRegistryName = blockRegistryName;
        return this;
    }

    /**
     * Returns a default suffix for {@link #blockRegistryName}. Note that this will be overridden if the {@link
     * #blockRegistryName} is changed in the Json.
     *
     * @return A default suffix for {@link #blockRegistryName}.
     */
    protected String getBlockRegistryNameSuffix() {
        return "_cap";
    }

    protected String getCenterBlockRegistryNameSuffix() {
        return "_cap_center";
    }

    public Optional<DynamicCapBlock> getDynamicCapBlock() {
        Block block = this.getDynamicCapState(false).getBlock();
        return Optional.ofNullable(block instanceof DynamicCapBlock ? (DynamicCapBlock) block : null);
    }

    public Optional<DynamicCapCenterBlock> getDynamicCapCenterBlock() {
        Block block = this.getDynamicCapState(true).getBlock();
        return Optional.ofNullable(block instanceof DynamicCapCenterBlock ? (DynamicCapCenterBlock) block : null);
    }

    protected DynamicCapBlock createDynamicCap(final BlockBehaviour.Properties properties) {
        return new DynamicCapBlock(this, properties);
    }

    protected DynamicCapCenterBlock createDynamicCapCenter(final BlockBehaviour.Properties properties) {
        return new DynamicCapCenterBlock(this, properties);
    }


    public void generateDynamicCapBlocks(final BlockBehaviour.Properties properties) {
        RegistryHandler.addBlock(this.blockRegistryName, () -> this.createDynamicCap(properties));
        RegistryHandler.addBlock(this.centerBlockRegistryName, () -> this.createDynamicCapCenter(properties));
    }

    public CapProperties setDynamicCapState(BlockState state, boolean center) {
        if (center) {
            dynamicMushroomCenterBlock = state;
            return this;
        }
        //Cache all the blockStates to speed up worldgen
        dynamicMushroomBlockDistanceStates[0] = Blocks.AIR.defaultBlockState();
        for (int i = 1; i <= MushroomCapDisc.MAX_RADIUS; i++) {
            dynamicMushroomBlockDistanceStates[i] = state.setValue(DynamicCapBlock.DISTANCE, i);
        }
        return this;
    }

    public BlockState getDynamicCapState(boolean center) {
        return getDynamicCapState(center, 1);
    }

    public BlockState getDynamicCapState(boolean center, int prop) {
        if (center) return dynamicMushroomCenterBlock.setValue(DynamicCapCenterBlock.AGE, Math.min(prop, MushroomCapDisc.MAX_RADIUS));
        return getDynamicCapState(prop);
    }

    public BlockState getDynamicCapState(int distance) {
        return Optional.ofNullable(dynamicMushroomBlockDistanceStates[Mth.clamp(distance, 0, MushroomCapDisc.MAX_RADIUS)])
                .orElse(Blocks.AIR.defaultBlockState());
    }

    public BlockState getDynamicCapState(int distance, boolean[] directions) {
        return DynamicCapBlock.setDirectionValues(getDynamicCapState(distance), directions);
    }

    public boolean isPartOfCap(BlockState state) {
        DynamicCapBlock capBlock = getDynamicCapBlock().orElse(null);
        DynamicCapCenterBlock capCenterBlock = getDynamicCapCenterBlock().orElse(null);
        if (capBlock == null || capCenterBlock == null) return false;
        return state.is(capBlock) || state.is(capCenterBlock);
    }

    public ParticleOptions sporeParticleType(BlockState state, Level level, BlockPos pos, RandomSource random) {
        return ParticleTypes.WHITE_ASH;
    }

    public Vec3 sporeParticleSpeed(BlockState state, Level level, BlockPos pos, RandomSource random) {
        return new Vec3(1, 0, 1);
    }

    ///////////////////////////////////////////
    // LOOT
    ///////////////////////////////////////////

    private Item mushroomItem;
    public final Item getMushroomItem() {
        if (this.mushroomItem == null) {
            LogManager.getLogger().warn("Invoked too early or item was not set on \"" + this.getRegistryName() + "\".");
            return Items.AIR;
        } else {
            return this.mushroomItem;
        }
    }
    public void setMushroomItem(Item mushroomItem) {
        this.mushroomItem = mushroomItem;
    }

    private final LootTableSupplier blockLootTableSupplier;

    public ResourceLocation getBlockLootTableName() {
        return blockLootTableSupplier.getName();
    }

    public boolean shouldGenerateBlockDrops() {
        return shouldGenerateDrops();
    }

    public LootTable.Builder createBlockDrops(HolderLookup.Provider registries) {
        if (getPrimitiveCapBlock().isPresent()) {
            return DTPLootTableHandler.createCapBlockDrops(primitiveCap.getBlock(), getMushroomItem(), -6, 2, registries);
        }
        return DTPLootTableHandler.createCapDrops(primitiveCap.getBlock(), getMushroomItem(), LootContextParamSets.BLOCK, registries);
    }

    private final LootTableSupplier lootTableSupplier;

    public ResourceLocation getLootTableName() {
        return lootTableSupplier.getName();
    }

    public LootTable getLootTable(ReloadableServerRegistries.Holder lootTables, Species species) {
        return lootTableSupplier.get(lootTables, species);
    }

    public boolean shouldGenerateDrops() {
        return getPrimitiveCapBlock().isPresent();
    }

    public LootTable.Builder createDrops(HolderLookup.Provider registries) {
        return DTPLootTableHandler.createCapDrops(primitiveCap.getBlock(), getMushroomItem(), DTLootParameterSets.LEAVES, registries);
    }

    public List<ItemStack> getDrops(Level level, BlockPos pos, ItemStack tool, Species species) {
        if (level.isClientSide) {
            return Collections.emptyList();
        }
        return getLootTable(Objects.requireNonNull(level.getServer()).reloadableRegistries(), species)
                .getRandomItems(createLootParams(level, pos, tool, species));
    }

    private LootParams createLootParams(Level level, BlockPos pos, ItemStack tool, Species species) {
        return new LootParams.Builder(LevelContext.getServerLevelOrThrow(level))
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(pos))
                .withParameter(DTLootContextParams.SPECIES, species)
                .withParameter(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR, species.seasonalSeedDropFactor(LevelContext.create(level), pos))
                .withParameter(LootContextParams.TOOL, tool)
                .create(DTLootParameterSets.LEAVES);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    public int getRadiusForConnection(BlockState state, BlockGetter blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        if (from.getFamily() != family) return 0;
        return fromRadius;
    }

    protected final MutableLazyValue<Generator<DTDataProvider.BlockState, CapProperties>> capStateGenerator =
            MutableLazyValue.supplied(CapStateGenerator::new);
    protected final MutableLazyValue<Generator<DTDataProvider.BlockState, CapProperties>> capCenterStateGenerator =
            MutableLazyValue.supplied(CapCenterStateGenerator::new);

    ///////////////////////////////////////////
    // DATA GENERATION
    ///////////////////////////////////////////

    @Override
    public void generateStateData(DTDataProvider.BlockState provider) {
        // Generate cap block state and model.
        this.capStateGenerator.get().generate(provider, this);
        this.capCenterStateGenerator.get().generate(provider, this);
    }

    public String getCapCenterAgeZeroModelName() {
        return "block/mushroom/" + centerBlockRegistryName.getPath() + "_age0";
    }
    public String getCapFaceModelName() {
        return "block/mushroom/" + blockRegistryName.getPath();
    }
    public String getCapInsideFaceModelName() {
        return "block/mushroom/" + blockRegistryName.getPath() + "_inside";
    }

    public ResourceLocation getCapCenterAgeZeroModelParent() {
        return getModelPath(CAP_CENTER_AGE_0_PARENT).orElse(ResourceLocation.parse("block/cube_bottom_top"));
    }
    public ResourceLocation getFaceModelParent() {
        return getModelPath(FACE).orElse(ResourceLocation.parse("block/template_single_face"));
    }

    private boolean generateFaceModels = false;

    public void setGenerateFaceModels(boolean generateFaceModels) {
        this.generateFaceModels = generateFaceModels;
    }

    public boolean shouldGenerateFaceModels() {
        return generateFaceModels;
    }

    public void addCapCenterAgeZeroTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                            ResourceLocation outsideTextureLocation, ResourceLocation insideTextureLocation) {
        ResourceLocation outLoc = getTexturePath(OUTSIDE_FACE).orElse(outsideTextureLocation);
        ResourceLocation inLoc = getTexturePath(INSIDE_FACE).orElse(insideTextureLocation);
        textureConsumer.accept("top", outLoc);
        textureConsumer.accept("bottom", inLoc);
        textureConsumer.accept("side", outLoc);
    }

    public void addCapFaceTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                            ResourceLocation textureLocation, boolean isInside) {
        ResourceLocation faceLoc = getTexturePath(isInside?INSIDE_FACE:OUTSIDE_FACE).orElse(textureLocation);
        textureConsumer.accept("texture", faceLoc);
    }

    protected HashMap<String, ResourceLocation> textureOverrides = new HashMap<>();
    protected HashMap<String, ResourceLocation> modelOverrides = new HashMap<>();
    public static final String OUTSIDE_FACE = "outside_face";
    public static final String INSIDE_FACE = "inside_face";
    public static final String FACE = "face";
    public static final String CAP_CENTER_AGE_0_PARENT = "cap_center_age_0_parent";

    public void setTextureOverrides(Map<String, ResourceLocation> textureOverrides) {
        this.textureOverrides.putAll(textureOverrides);
    }
    public void setModelOverrides(Map<String, ResourceLocation> modelOverrides) {
        this.modelOverrides.putAll(modelOverrides);
    }
    public Optional<ResourceLocation> getTexturePath(String key) {
        return Optional.ofNullable(textureOverrides.getOrDefault(key, null));
    }
    public Optional<ResourceLocation> getModelPath(String key) {
        return Optional.ofNullable(modelOverrides.getOrDefault(key, null));
    }

}
