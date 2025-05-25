package com.dtteam.dynamictreesplus.worldgen.structure;

import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.structure.RegularTemplatePoolModifier;
import com.dtteam.dynamictrees.worldgen.structure.TreePoolElement;
import com.dtteam.dynamictreesplus.init.DTPCacti;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID;
import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING;


/**
 * @author Harley O'Connor
 */
public final class VillageCactusReplacement {

    public static void replaceCactiFromVanillaVillages(HolderLookup.Provider vanillaProvider, BootstrapContext<StructureTemplatePool> context) {
        // Replace cacti from Desert village.
        final TreePoolElement cactusElement = new TreePoolElement(Species.REGISTRY.get(DTPCacti.PILLAR), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(vanillaProvider, "desert", "decor").replaceTemplate(1, cactusElement).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "desert", "zombie/decor").replaceTemplate(1, cactusElement).registerPool(context);

        // Replace cactus in small desert village house.
        final LegacySinglePoolElement houseTemplate = StructurePoolElement.legacy("dynamictreesplus:village/desert/houses/desert_small_house_7")
                .apply(RIGID);
        RegularTemplatePoolModifier.village(vanillaProvider, "desert", "houses").replaceTemplate(6, houseTemplate).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "desert", "zombie/houses").replaceTemplate(6, houseTemplate).registerPool(context);
    }

}
