package com.dtteam.dynamictreesplus.resources;

import com.dtteam.dynamictrees.api.configuration.ConfigurationTemplateResourceLoader;
import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.deserializer.ResourceLocationDeserializer;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.dtteam.dynamictreesplus.block.mushroom.CapProperties;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.MushroomShapeConfiguration;
import com.dtteam.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CapPropertiesResourceLoader extends JsonRegistryResourceLoader<CapProperties> {

    public static final CapPropertiesResourceLoader CAP_PROPERTIES_LOADER = new CapPropertiesResourceLoader();
    public static ConfigurationTemplateResourceLoader<MushroomShapeConfiguration, MushroomShapeKit>
            MUSHROOM_SHAPE_KIT_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "mushroom_shape_kits/configurations",
            MushroomShapeKit.REGISTRY,
            MushroomShapeConfiguration.TEMPLATES
    );

    public static final String CAP_PROPERTIES = "cap_properties";

    public CapPropertiesResourceLoader() {
        super(CapProperties.REGISTRY, CAP_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        // Primitive leaves are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_cap", Block.class, CapProperties::setPrimitiveCap)
                .register("generate_face_models", Boolean.class, CapProperties::setGenerateFaceModels)
                .registerMapApplier("texture_overrides", ResourceLocation.class, CapProperties::setTextureOverrides)
                .registerMapApplier("model_overrides", ResourceLocation.class, CapProperties::setModelOverrides)
                .register("mushroom_item", Item.class, CapProperties::setMushroomItem);

        // Primitive leaves are needed both client and server (so cannot be done on load).
        this.setupAppliers.register("primitive_cap", Block.class, CapProperties::setPrimitiveCap)
                .register("family", ResourceLocation.class, (capProperties, registryName) -> {
                    final ResourceLocation processedRegName = ResourceLocationUtils.parseDTLocation(registryName);
                    Family.REGISTRY.runOnNextLock(Family.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            capProperties::setFamily,
                            () -> this.logWarning(capProperties.getRegistryName(),
                                    "Could not set family for cap properties with name \"" + capProperties
                                            + "\" as family \"" + processedRegName + "\" was not found.")
                    ));
                });

        this.reloadAppliers.register("fire_spread", Integer.class, CapProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, CapProperties::setFlammability)
                .register("age_zero_shape", VoxelShape.class, CapProperties::setAgeZeroShape);

        super.registerAppliers();
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<CapProperties>.LoadData loadData, JsonObject json) {
        final CapProperties capProperties = loadData.getResource();
        this.readCustomBlockRegistryName(capProperties, json);

        if (this.shouldGenerateBlocks(json)) {
            this.generateBlocks(capProperties, json);
        }

        super.applyLoadAppliers(loadData, json);
    }

    private void readCustomBlockRegistryName(CapProperties capProperties, JsonObject json) {
        JsonResult.forInput(json)
                .mapIfContains("block_registry_name", JsonElement.class, input ->
                        ResourceLocationDeserializer.create(capProperties.getRegistryName().getNamespace())
                                .deserialize(input).orElseThrow(), capProperties.getBlockRegistryName()
                ).ifSuccessOrElse(
                        capProperties::setBlockRegistryName,
                        error -> this.logError(capProperties.getRegistryName(), error),
                        warning -> this.logWarning(capProperties.getRegistryName(), warning)
                ).mapIfContains("center_block_registry_name", JsonElement.class, input ->
                        ResourceLocationDeserializer.create(capProperties.getRegistryName().getNamespace())
                                .deserialize(input).orElseThrow(), capProperties.getCenterBlockRegistryName()
                ).ifSuccessOrElse(
                        capProperties::setCenterBlockRegistryName,
                        error -> this.logError(capProperties.getRegistryName(), error),
                        warning -> this.logWarning(capProperties.getRegistryName(), warning)
                );
    }

    private Boolean shouldGenerateBlocks(JsonObject json) {
        return JsonHelper.getOrDefault(json, "generate_block", Boolean.class, true);
    }

    public MapColor getDefaultMapColor() {
        return MapColor.PLANT;
    }

    private void generateBlocks(CapProperties capProperties, JsonObject json) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                json,
                capProperties.getDefaultMapColor(),
                capProperties::getDefaultBlockProperties,
                error -> this.logError(capProperties.getRegistryName(), error),
                warning -> this.logWarning(capProperties.getRegistryName(), warning)
        );

        capProperties.generateDynamicCapBlocks(blockProperties);
    }

}
