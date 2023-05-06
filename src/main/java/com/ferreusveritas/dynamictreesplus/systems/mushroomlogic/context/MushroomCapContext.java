package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic.context;

import com.ferreusveritas.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public record MushroomCapContext(LevelAccessor level, BlockPos pos, HugeMushroomSpecies species, Integer age) { }
