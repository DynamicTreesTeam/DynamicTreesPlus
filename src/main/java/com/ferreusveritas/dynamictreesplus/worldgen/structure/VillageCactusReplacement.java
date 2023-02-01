package com.ferreusveritas.dynamictreesplus.worldgen.structure;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.worldgen.structure.RegularTemplatePoolModifier;
import com.ferreusveritas.dynamictrees.worldgen.structure.TreePoolElement;
import com.ferreusveritas.dynamictreesplus.init.DTPCacti;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID;
import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING;

/**
 * @author Harley O'Connor
 */
public final class VillageCactusReplacement {

    public static void replaceCactiFromVanillaVillages() {
        // Replace cacti from Desert village.
        final TreePoolElement cactusElement = new TreePoolElement(Species.REGISTRY.get(DTPCacti.PILLAR), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village("desert", "decor")
                .replaceTemplate(1, cactusElement);
        RegularTemplatePoolModifier.village("desert", "zombie/decor")
                .replaceTemplate(1, cactusElement);

        // Replace cactus in small desert village house.
        final LegacySinglePoolElement houseTemplate = StructurePoolElement.legacy("dynamictreesplus:village/desert/houses/desert_small_house_7")
                .apply(RIGID);
        RegularTemplatePoolModifier.village("desert", "houses")
                .replaceTemplate(6, houseTemplate);
        RegularTemplatePoolModifier.village("desert", "zombie/houses")
                .replaceTemplate(6, houseTemplate);
    }

}
