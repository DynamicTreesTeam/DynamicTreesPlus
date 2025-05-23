package com.dtteam.dynamictreesplus.systems.mushroomlogic.context;

import com.dtteam.dynamictreesplus.tree.HugeMushroomSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public record MushroomCapContext(LevelAccessor level, BlockPos pos, HugeMushroomSpecies species, Integer age) { }
