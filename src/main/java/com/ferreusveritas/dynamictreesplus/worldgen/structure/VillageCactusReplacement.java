package com.ferreusveritas.dynamictreesplus.worldgen.structure;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.structure.RegularPatternModifier;
import com.ferreusveritas.dynamictrees.worldgen.structure.TreeJigsawPiece;
import com.ferreusveritas.dynamictreesplus.init.DTPCacti;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.LegacySingleJigsawPiece;

/**
 * @author Harley O'Connor
 */
public final class VillageCactusReplacement {

    public static void replaceTreesFromVanillaVillages() {
        // Replace cacti from Desert village.
        final TreeJigsawPiece cactusTemplate = new TreeJigsawPiece(Species.REGISTRY.get(DTPCacti.PILLAR),
                JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);
        RegularPatternModifier.village("desert", "decor")
                .replaceTemplate(1, cactusTemplate);
        RegularPatternModifier.village("desert", "zombie/decor")
                .replaceTemplate(1, cactusTemplate);

        // Replace cactus in small desert village house.
        final LegacySingleJigsawPiece houseTemplate =
                JigsawPiece.legacy("dynamictreesplus:village/desert/houses/desert_small_house_7")
                        .apply(JigsawPattern.PlacementBehaviour.RIGID);
        RegularPatternModifier.village("desert", "houses")
                .replaceTemplate(6, houseTemplate);
        RegularPatternModifier.village("desert", "zombie/houses")
                .replaceTemplate(6, houseTemplate);
    }

}
