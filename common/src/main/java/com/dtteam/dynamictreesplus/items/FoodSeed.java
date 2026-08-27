package com.dtteam.dynamictreesplus.items;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FoodSeed extends Seed {

    public static final FoodProperties SAGUARO_FRUIT = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();

    public FoodSeed(Identifier id, Species species) {
        super(species, new Item.Properties().food(SAGUARO_FRUIT).setId(ResourceKey.create(Registries.ITEM, id)));
    }

    /**NeoForge Override*/ @SuppressWarnings("unused")
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        if (source.is(DamageTypes.CACTUS))
            return false;
        return stack.canBeHurtBy(source);
    }

}
