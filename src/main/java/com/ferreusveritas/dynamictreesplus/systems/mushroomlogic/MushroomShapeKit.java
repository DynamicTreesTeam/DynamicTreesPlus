package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.cell.LeafClusters;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.resources.ResourceLocation;

public abstract class MushroomShapeKit extends RegistryEntry<MushroomShapeKit> {

    public static final MushroomShapeKit NULL_MUSHROOM_SHAPE_KIT = new MushroomShapeKit(DTTrees.NULL) {
        @Override
        public int getDefaultDistance() {
            return 0;
        }

        @Override
        public SimpleVoxmap getShapeCluster() {
            return MushroomShapeClusters.NULL_MAP;
        }
    };

    /**
     * Central registry for all {@link MushroomShapeKit} objects.
     */
    public static final SimpleRegistry<MushroomShapeKit> REGISTRY = new SimpleRegistry<>(MushroomShapeKit.class, NULL_MUSHROOM_SHAPE_KIT);

    public MushroomShapeKit(final ResourceLocation registryName) {
        this.setRegistryName(registryName);
    }

    /**
     * The default distance value of a newly created mushroom cap block [default = 4]
     **/
    public abstract int getDefaultDistance();

    public abstract SimpleVoxmap getShapeCluster();

}
