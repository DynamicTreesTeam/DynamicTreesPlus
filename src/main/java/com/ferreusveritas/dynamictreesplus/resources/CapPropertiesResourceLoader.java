package com.ferreusveritas.dynamictreesplus.resources;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictreesplus.block.mushroom.CapProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CapPropertiesResourceLoader extends JsonRegistryResourceLoader<CapProperties> {
    public static final CapPropertiesResourceLoader CAP_PROPERTIES_LOADER = new CapPropertiesResourceLoader();

    public static final String CAP_PROPERTIES = "cap_properties";

    public CapPropertiesResourceLoader() {
        super(CapProperties.REGISTRY, CAP_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        // Primitive leaves are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_cap", Block.class, CapProperties::setPrimitiveCap)
                .registerListApplier("mushroom_drop_chances", Float.class, CapProperties::setMushroomDropChances);

        // Primitive leaves are needed both client and server (so cannot be done on load).
        this.setupAppliers.register("primitive_cap", Block.class, CapProperties::setPrimitiveCap)
                .register("family", ResourceLocation.class, (capProperties, registryName) -> {
                    final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
                    Family.REGISTRY.runOnNextLock(Family.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            capProperties::setFamily,
                            () -> this.logWarning(capProperties.getRegistryName(),
                                    "Could not set family for cap properties with name \"" + capProperties
                                            + "\" as family \"" + processedRegName + "\" was not found.")
                    ));
                });

//        this.reloadAppliers.register("fire_spread", Integer.class, CapProperties::setFireSpreadSpeed)
//                .register("flammability", Integer.class, CapProperties::setFlammability);

        super.registerAppliers();
    }

}
