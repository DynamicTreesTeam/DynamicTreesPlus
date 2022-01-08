package com.ferreusveritas.dynamictreesplus.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorConfiguration;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import net.minecraft.util.ResourceLocation;

/**
 * @author ferreusveritas
 */
public class CactusSeedDropCreator extends DropCreator {

    public static final ConfigurationProperty<Float> SEED_PER_BRANCH = ConfigurationProperty.floatProperty("seeds_per_branch");

    public CactusSeedDropCreator(ResourceLocation name) {
        super(name);
    }

    @Override
    protected void registerProperties() {
        this.register(SEED_PER_BRANCH);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(SEED_PER_BRANCH, 0.5f);
    }

    @Override
    public void appendLogDrops(DropCreatorConfiguration configuration, LogDropContext context) {
        int numLogs = (int) (context.volume().getVolume() * configuration.get(SEED_PER_BRANCH));
        while (numLogs > 0) {
            context.drops().add(context.species().getSeedStack(Math.min(numLogs, 64)));
            numLogs -= 64;
        }
        super.appendLogDrops(configuration, context);
    }

}
