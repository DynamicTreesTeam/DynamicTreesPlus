package com.dtteam.dynamictreesplus.items;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FoodSeed extends Seed {

    public static final FoodProperties SAGUARO_FRUIT = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();

    public FoodSeed(Species species) {
        super(species, new Item.Properties().food(SAGUARO_FRUIT));
    }

    /**NeoForge Override*/ @SuppressWarnings("unused")
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        if (source.is(DamageTypes.CACTUS))
            return false;
        return stack.canBeHurtBy(source);
    }

}
