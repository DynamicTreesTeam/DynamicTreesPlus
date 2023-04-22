package com.ferreusveritas.dynamictreesplus.block.mushroom;

import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.tree.Resettable;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Iterator;

public class CapProperties extends RegistryEntry<CapProperties> implements Resettable<CapProperties> {

    public static final Codec<CapProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(CapProperties::getRegistryName))
            .apply(instance, CapProperties::new));

    public static final CapProperties NULL = new CapProperties() {

    };

    /**
     * Central registry for all {@link CapProperties} objects.
     *
     * TO-DO: make it work with the RegistryCommand
     */
    public static final TypedRegistry<CapProperties> REGISTRY = new TypedRegistry<>(CapProperties.class, NULL, new TypedRegistry.EntryType<>(CODEC));

    private CapProperties() {
//        this.blockLootTableSupplier = new LootTableSupplier("null/", DTTrees.NULL);
//        this.lootTableSupplier = new LootTableSupplier("null/", DTTrees.NULL);
    }

    public CapProperties(final ResourceLocation registryName) {
        //this(null, registryName);
    }

    protected static final int maxDistance = 6;
    /**
     * The primitive (vanilla) mushroom block is used for many purposes including rendering, drops, and some other basic
     * behavior.
     */
    protected BlockState primitiveCap;
//    /**
//     * The {@link MushroomShapeKit}, which is for leaves automata.
//     */
//    protected MushroomShapeKit mushroomShapeKit;
    protected Family family;
    protected BlockState[] dynamicMushroomBlockDistanceStates = new BlockState[maxDistance + 1];
    protected int flammability = 0;// Mimic vanilla mushroom
    protected int fireSpreadSpeed = 0;// Mimic vanilla mushroom

    ///////////////////////////////////////////
    // PROPERTIES
    ///////////////////////////////////////////

    public void setPrimitiveCap(final Block primitiveCap) {
        if (this.primitiveCap == null || primitiveCap != this.primitiveCap.getBlock()) {
            this.primitiveCap = primitiveCap.defaultBlockState();
        }
    }

    public CapProperties setFamily(Family family) {
        this.family = family;
        if (family.isFireProof()) {
            flammability = 0;
            fireSpreadSpeed = 0;
        }
        return this;
    }

    ///////////////////////////////////////////
    // LOOT
    ///////////////////////////////////////////

    /**
     * Chances for leaves to drop seeds. Used in data gen for loot tables.
     */
    protected float[] mushroomDropChances = new float[]{0.015625F, 0.03125F, 0.046875F, 0.0625F};

    public void setMushroomDropChances(float[] mushroomDropChances) {
        this.mushroomDropChances = (float[]) mushroomDropChances;
    }

    public void setMushroomDropChances(Collection<Float> mushroomDropChances) {
        this.mushroomDropChances = new float[mushroomDropChances.size()];
        Iterator<Float> iterator = mushroomDropChances.iterator();
        for (int i = 0; i < mushroomDropChances.size(); i++) {
            this.mushroomDropChances[i] = iterator.next();
        }
    }

}
