package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.tree.Resettable;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.*;
import com.ferreusveritas.dynamictreesplus.data.CapStateGenerator;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.mojang.math.Vector3d;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;

public class CapProperties extends RegistryEntry<CapProperties> implements Resettable<CapProperties> {

    public static final Codec<CapProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(CapProperties::getRegistryName))
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
        public MushroomShapeConfiguration getMushroomShapeKit() {
            return MushroomShapeKit.NULL.getDefaultConfiguration();
        }

        @Override
        public int getFlammability() {
            return 0;
        }

        @Override
        public int getFireSpreadSpeed() {
            return 0;
        }
    }.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

    /**
     * Central registry for all {@link CapProperties} objects.
     *
     * TO-DO: make it work with the RegistryCommand
     */
    public static final TypedRegistry<CapProperties> REGISTRY = new TypedRegistry<>(CapProperties.class, NULL, new TypedRegistry.EntryType<>(CODEC));

    private CapProperties() {
        this.blockLootTableSupplier = new LootTableSupplier("null/", DTTrees.NULL);
        this.lootTableSupplier = new LootTableSupplier("null/", DTTrees.NULL);
    }

    public CapProperties(final ResourceLocation registryName) {
        this(null, registryName);
    }

    public CapProperties(@Nullable final BlockState primitiveCap, final ResourceLocation registryName) {
        this(primitiveCap, MushroomShapeKit.NULL.getDefaultConfiguration(), registryName);
    }

    public CapProperties(@Nullable final BlockState primitiveCap, final MushroomShapeConfiguration shapeKit, final ResourceLocation registryName) {
        this.family = Family.NULL_FAMILY;
        this.primitiveCap = primitiveCap != null ? primitiveCap : BlockStates.AIR;
        this.mushroomShapeKit = shapeKit;
        this.setRegistryName(registryName);
        this.centerBlockRegistryName = ResourceLocationUtils.suffix(registryName, this.getCenterBlockRegistryNameSuffix());
        this.blockRegistryName = ResourceLocationUtils.suffix(registryName, this.getBlockRegistryNameSuffix());
        this.blockLootTableSupplier = new LootTableSupplier("blocks/", blockRegistryName);
        this.lootTableSupplier = new LootTableSupplier("trees/mushroom_caps/", registryName);
    }

    protected int maxDistance = 6;
    protected int maxAge = 6;
    /**
     * The primitive (vanilla) mushroom block is used for many purposes including rendering, drops, and some other basic
     * behavior.
     */
    protected BlockState primitiveCap;
    /**
     * The {@link MushroomShapeKit}, which is for leaves automata.
     */
    protected MushroomShapeConfiguration mushroomShapeKit;
    protected Family family;
    protected BlockState[] dynamicMushroomBlockDistanceStates = new BlockState[getMaxDistance() + 1];
    protected BlockState dynamicMushroomCenterBlock;
    protected int flammability = 0;// Mimic vanilla mushroom
    protected int fireSpreadSpeed = 0;// Mimic vanilla mushroom
    protected float chanceToAge = 0.75f;
    protected VoxelShape ageZeroShape = Shapes.block();

    ///////////////////////////////////////////
    // PROPERTIES
    ///////////////////////////////////////////
    /**
     * Gets the primitive (vanilla) leaves for these {@link LeavesProperties}.
     *
     * @return The {@link BlockState} for the primitive leaves.
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
     * Gets {@link ItemStack} of the primitive (vanilla) leaves (for things like when it's sheared).
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

    /**
     * Gets the {@link CellKit}, which is for leaves automata.
     *
     * @return The {@link CellKit} object.
     */
    public MushroomShapeConfiguration getMushroomShapeKit() {
        return mushroomShapeKit;
    }

    public void setMushroomShapeConfiguration(MushroomShapeConfiguration mushroomShapeKit) {
        this.mushroomShapeKit = mushroomShapeKit;
    }

    public Material getDefaultMaterial() {
        return Material.WOOD;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(final Material material, final MaterialColor materialColor) {
        return BlockBehaviour.Properties.of(material, materialColor)
                .strength(0.2F)
                .sound(SoundType.WOOD);
    }

    public float getChanceToAge() {
        return chanceToAge;
    }

    public void setChanceToAge(float chanceToAge) {
        this.chanceToAge = chanceToAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
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

    public void generateDynamicCap(final BlockBehaviour.Properties properties) {
        RegistryHandler.addBlock(this.blockRegistryName, () -> this.createDynamicCap(properties));
    }

    public void generateDynamicCapCenter(final BlockBehaviour.Properties properties) {
        RegistryHandler.addBlock(this.centerBlockRegistryName, () -> this.createDynamicCapCenter(properties));
    }

    public CapProperties setDynamicCapState(BlockState state, boolean center) {
        if (center){
            dynamicMushroomCenterBlock = state;
            return this;
        }
        //Cache all the blockStates to speed up worldgen
        dynamicMushroomBlockDistanceStates[0] = Blocks.AIR.defaultBlockState();
        for (int i = 1; i <= getMaxDistance(); i++) {
            dynamicMushroomBlockDistanceStates[i] = state.setValue(DynamicCapBlock.DISTANCE, i);
        }
        return this;
    }

    public BlockState getDynamicCapState(boolean center) {
        if (center) return dynamicMushroomCenterBlock;
        return getDynamicCapState(getMushroomShapeKit().getDefaultDistance());
    }

    public BlockState getDynamicCapState(int distance) {
        return Optional.ofNullable(dynamicMushroomBlockDistanceStates[Mth.clamp(distance, 0, getMaxDistance())])
                .orElse(Blocks.AIR.defaultBlockState());
    }

    public BlockState getDynamicCapState(int distance, boolean[] directions){
        return DynamicCapBlock.setDirectionValues(getDynamicCapState(distance), directions);
    }

    public boolean isPartOfCap(BlockState state){
        DynamicCapBlock capBlock = getDynamicCapBlock().orElse(null);
        DynamicCapCenterBlock capCenterBlock = getDynamicCapCenterBlock().orElse(null);
        if (capBlock == null || capCenterBlock == null) return false;
        return state.is(capBlock) || state.is(capCenterBlock);
    }

    public ParticleOptions sporeParticleType (BlockState pState, Level pLevel, BlockPos pPos, Random pRand){
        return ParticleTypes.WHITE_ASH;
    }
    public Vector3d sporeParticleSpeed (BlockState pState, Level pLevel, BlockPos pPos, Random pRand){
        return new Vector3d(1,0,1);
    }

    ///////////////////////////////////////////
    // LOOT
    ///////////////////////////////////////////

    /**
     * Chances for leaves to drop seeds. Used in data gen for loot tables.
     */
    protected float[] mushroomDropChances = new float[]{0.015625F, 0.03125F, 0.046875F, 0.0625F};

    public void setMushroomDropChances(float[] mushroomDropChances) {
        this.mushroomDropChances = mushroomDropChances;
    }

    public void setMushroomDropChances(Collection<Float> mushroomDropChances) {
        this.mushroomDropChances = new float[mushroomDropChances.size()];
        Iterator<Float> iterator = mushroomDropChances.iterator();
        for (int i = 0; i < mushroomDropChances.size(); i++) {
            this.mushroomDropChances[i] = iterator.next();
        }
    }

    private final LootTableSupplier blockLootTableSupplier;

    public ResourceLocation getBlockLootTableName() {
        return blockLootTableSupplier.getName();
    }

    public LootTable getBlockLootTable(LootTables lootTables, Species species) {
        return blockLootTableSupplier.get(lootTables, species);
    }

    public boolean shouldGenerateBlockDrops() {
        return shouldGenerateDrops();
    }

    public LootTable.Builder createBlockDrops() {
        if (mushroomDropChances != null && getPrimitiveCapBlock().isPresent()) {
            return DTLootTableProvider.createLeavesBlockDrops(primitiveCap.getBlock(), mushroomDropChances);
        }
        return DTLootTableProvider.createLeavesDrops(mushroomDropChances, LootContextParamSets.BLOCK);
    }

    private final LootTableSupplier lootTableSupplier;

    public ResourceLocation getLootTableName() {
        return lootTableSupplier.getName();
    }

    public LootTable getLootTable(LootTables lootTables, Species species) {
        return lootTableSupplier.get(lootTables, species);
    }

    public boolean shouldGenerateDrops() {
        return getPrimitiveCapBlock().isPresent();
    }

    public LootTable.Builder createDrops() {
        return DTLootTableProvider.createLeavesDrops(mushroomDropChances, DTLootParameterSets.LEAVES);
    }

    public List<ItemStack> getDrops(Level level, BlockPos pos, ItemStack tool, Species species) {
        if (level.isClientSide) {
            return Collections.emptyList();
        }
        return getLootTable(Objects.requireNonNull(level.getServer()).getLootTables(), species)
                .getRandomItems(createLootContext(level, pos, tool, species));
    }

    private LootContext createLootContext(Level level, BlockPos pos, ItemStack tool, Species species) {
        return new LootContext.Builder(LevelContext.getServerLevelOrThrow(level))
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

    protected final MutableLazyValue<Generator<DTBlockStateProvider, CapProperties>> stateGenerator =
            MutableLazyValue.supplied(CapStateGenerator::new);

    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        // Generate cap block state and model.
        this.stateGenerator.get().generate(provider, this);
    }

}
