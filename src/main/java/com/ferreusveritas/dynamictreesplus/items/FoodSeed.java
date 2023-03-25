package com.ferreusveritas.dynamictreesplus.items;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;

public class FoodSeed extends Seed {

    public static final Food SAGUARO_FRUIT = (new Food.Builder()).nutrition(3).saturationMod(0.4F).build();

    public FoodSeed(Species species) {
        super(species, new Item.Properties().food(SAGUARO_FRUIT));
    }

    @Override
    public boolean canBeHurtBy(@Nonnull DamageSource pDamageSource) {
        if (pDamageSource == DamageSource.CACTUS) return false;
        return super.canBeHurtBy(pDamageSource);
    }
}
