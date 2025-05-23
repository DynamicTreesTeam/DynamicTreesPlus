package com.dtteam.dynamictreesplus.items;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

public class FoodSeed extends Seed {

    public static final FoodProperties SAGUARO_FRUIT = (new FoodProperties.Builder()).nutrition(2).saturationMod(0.2F).build();

    public FoodSeed(Species species) {
        super(species, new Item.Properties().food(SAGUARO_FRUIT));
    }

    @Override
    public boolean canBeHurtBy(DamageSource pDamageSource) {
        if (pDamageSource.is(DamageTypes.CACTUS)) return false;
        return super.canBeHurtBy(pDamageSource);
    }
}
