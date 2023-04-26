package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.api.configuration.Configuration;
import com.ferreusveritas.dynamictrees.api.configuration.TemplateRegistry;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context.MushroomCapContext;
import com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.shapekits.MushroomShapeKit;
import net.minecraft.core.BlockPos;

import java.util.List;

public final class MushroomShapeConfiguration extends Configuration<MushroomShapeConfiguration, MushroomShapeKit> {

    public static final TemplateRegistry<MushroomShapeConfiguration> TEMPLATES = new TemplateRegistry<>();

    public MushroomShapeConfiguration(MushroomShapeKit configurable) {
        super(configurable);
    }

    public MushroomShapeKit getShapeKit() {
        return this.configurable;
    }

    @Override
    public MushroomShapeConfiguration copy() {
        final MushroomShapeConfiguration duplicateShapeKit = new MushroomShapeConfiguration(this.configurable);
        duplicateShapeKit.properties.putAll(this.properties);
        return duplicateShapeKit;
    }

    public static MushroomShapeConfiguration getDefault() {
        return MushroomShapeKit.NULL.getDefaultConfiguration();
    }

    public int getDefaultDistance (){
        return this.configurable.getDefaultDistance();
    }

    public void generateMushroomCap(MushroomCapContext context){
        this.configurable.generateMushroomCap(this, context);
    }

    public void clearMushroomCap(MushroomCapContext context){
        this.configurable.clearMushroomCap(this, context);
    }

    public List<BlockPos> getShapeCluster (MushroomCapContext context){
        return this.configurable.getShapeCluster(this, context);
    }

}
