package com.ferreusveritas.dynamictreesplus.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.ferreusveritas.dynamictreesplus.blocks.CactusBranchBlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CactusBranchBlockBakedModel extends BranchBlockBakedModel {

    private TextureAtlasSprite barkTexture;

    // Not as many baked models as normal branches, although each model has more quads. Still less quads in total, though.
    private final IBakedModel[][] sleeves = new IBakedModel[6][3];
    private final IBakedModel[][] cores = new IBakedModel[3][3]; // 3 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
    private final IBakedModel[] rings = new IBakedModel[3]; // 3 Cores with the ring textures on all 6 sides
    private final IBakedModel[] coreSpikes = new IBakedModel[3]; // 3 cores with only the spikey edges
    private IBakedModel sleeveTopSpikes;

    int[] radii = {4,5,7};

    public CactusBranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        super (modelResLoc, barkResLoc, ringsResLoc);
    }

    @Override
    public void setupModels () {
        this.barkTexture = ModelUtils.getTexture(this.barkResLoc);
        TextureAtlasSprite ringsTexture = ModelUtils.getTexture(this.ringsResLoc);

        for (int i = 0; i < 3; i++) {
            int radius = radii[i];

            for (Direction dir: Direction.values()) {
                sleeves[dir.get3DDataValue()][i] = bakeSleeve(radius, dir, barkTexture, ringsTexture);
            }

            cores[0][i] = bakeCore(radius, Axis.Y, barkTexture); //DOWN<->UP
            cores[1][i] = bakeCore(radius, Axis.Z, barkTexture); //NORTH<->SOUTH
            cores[2][i] = bakeCore(radius, Axis.X, barkTexture); //WEST<->EAST

            rings[i] = bakeCore(radius, Axis.Y, ringsTexture);

            coreSpikes[i] = bakeCoreSpikes(radius, barkTexture);
            sleeveTopSpikes = bakeTopSleeveSpikes(barkTexture);
        }
    }

    private void putVertex(BakedQuadBuilder builder, Vector3d normal, double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {

        final ImmutableList<VertexFormatElement> elements = DefaultVertexFormats.BLOCK.getElements().asList();
        for (int j = 0 ; j < elements.size() ; j++) {
            VertexFormatElement e = elements.get(j);
            switch (e.getUsage()) {
                case POSITION:
                    builder.put(j, (float) x, (float) y, (float) z, 1.0f);
                    break;
                case COLOR:
                    builder.put(j, r, g, b, 1.0f);
                    break;
                case UV:
                    switch (e.getIndex()) {
                        case 0:
                            float iu = sprite.getU(u);
                            float iv = sprite.getV(v);
                            builder.put(j, iu, iv);
                            break;
                        case 2:
                            builder.put(j, (short) 0, (short) 0);
                            break;
                        default:
                            builder.put(j);
                            break;
                    }
                    break;
                case NORMAL:
                    builder.put(j, (float) normal.x, (float) normal.y, (float) normal.z);
                    break;
                default:
                    builder.put(j);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vector3d v1, float v1u, float v1v, Vector3d v2, float v2u, float v2v, Vector3d v3, float v3u, float v3v, Vector3d v4, float v4u, float v4v, TextureAtlasSprite sprite) {
        Vector3d normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, v1u, v1v, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, v2u, v2v, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, v3u, v3v, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, v4u, v4v, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    public IBakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark, TextureAtlasSprite top) {
        // Work in double units(*2)
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getStepY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
        if (dir.getAxis() == Axis.Z) { // North/South
            negative = !negative;
        }

        Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face: Direction.values()) {
            if (dir.getOpposite() != face) { // Discard side of sleeve that faces core
                BlockFaceUV uvface = null;
                if (dir == face) { // Side of sleeve that faces away from core
                    if (radius == 4 || (radius == 5 && dir == Direction.DOWN)) {
                        uvface = new BlockFaceUV(new float[] {8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
                    }
                } else { // UV for Bark texture
                    uvface = new BlockFaceUV(new float[]{ 8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize }, getFaceAngle(dir.getAxis(), face));
                }
                if (uvface != null) {
                    mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
                }
            }
        }

        BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrideList.EMPTY).particle(bark);

        for (Map.Entry<Direction, BlockPartFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), (dir == face) ? top : bark, face, ModelRotation.X0_Y0, this.modelResLoc));
        }
        float minV = negative ? 16 - halfSize : 0;
        float maxV = negative ? 16 : halfSize;
        switch (dir.getAxis()) {
            case X:

                builder.addCulledFace(Direction.NORTH, this.createQuad(
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 16, minV,
                        v(posTo.x() / 16f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 14, minV,
                        v(posFrom.x() / 16f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 14, maxV,
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 16, maxV, bark));
                builder.addCulledFace(Direction.NORTH, this.createQuad(
                        v(posTo.x() / 16f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 2, minV,
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 0, minV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 0, maxV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 2, maxV, bark));
                builder.addCulledFace(Direction.SOUTH, this.createQuad(
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 16, maxV,
                        v(posFrom.x() / 16f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 14, maxV,
                        v(posTo.x() / 16f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 14, minV,
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 16, minV, bark));
                builder.addCulledFace(Direction.SOUTH, this.createQuad(
                        v(posFrom.x() / 16f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 2, maxV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 0, maxV,
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 0, minV,
                        v(posTo.x() / 16f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 2, minV, bark));

                builder.addCulledFace(Direction.DOWN, this.createQuad(
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f - 0.0625f), 14, minV,
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f + 0.0625f), 16, minV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f + 0.0625f), 16, maxV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f - 0.0625f), 14, maxV, bark));
                builder.addCulledFace(Direction.DOWN, this.createQuad(
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f - 0.0625f), 0, minV,
                        v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f + 0.0625f), 2, minV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f + 0.0625f), 2, maxV,
                        v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f - 0.0625f), 0, maxV, bark));
                builder.addCulledFace(Direction.UP, this.createQuad(
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f - 0.0625f), 14, maxV,
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f + 0.0625f), 16, maxV,
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f + 0.0625f), 16, minV,
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f - 0.0625f), 14, minV, bark));
                builder.addCulledFace(Direction.UP, this.createQuad(
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f - 0.0625f), 2, maxV,
                        v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f + 0.0625f), 0, maxV,
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f + 0.0625f), 0, minV,
                        v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f - 0.0625f), 2, minV, bark));

                break;
            case Y:

                builder.addCulledFace(Direction.WEST, this.createQuad(
                        v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posTo.z() / 16f + 0.0625f), 16, minV,
                        v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posTo.z() / 16f - 0.0625f), 14, minV,
                        v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posTo.z() / 16f - 0.0625f), 14, maxV,
                        v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posTo.z() / 16f + 0.0625f), 16, maxV, bark));
                builder.addCulledFace(Direction.WEST, this.createQuad(
                        v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, minV,
                        v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, minV,
                        v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, maxV,
                        v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, maxV, bark));
                builder.addCulledFace(Direction.EAST, this.createQuad(
                        v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posTo.z() / 16f + 0.0625f), 16, maxV,
                        v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posTo.z() / 16f - 0.0625f), 14, maxV,
                        v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posTo.z() / 16f - 0.0625f), 14, minV,
                        v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posTo.z() / 16f + 0.0625f), 16, minV, bark));
                builder.addCulledFace(Direction.EAST, this.createQuad(
                        v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, maxV,
                        v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, maxV,
                        v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, minV,
                        v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, minV, bark));

                builder.addCulledFace(Direction.NORTH, this.createQuad(
                        v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 16, maxV,
                        v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 14, maxV,
                        v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 14, minV,
                        v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 16, minV, bark));
                builder.addCulledFace(Direction.NORTH, this.createQuad(
                        v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 2, maxV,
                        v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 0, maxV,
                        v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 0, minV,
                        v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 2, minV, bark));
                builder.addCulledFace(Direction.SOUTH, this.createQuad(
                        v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 16, minV,
                        v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 14, minV,
                        v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 14, maxV,
                        v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 16, maxV, bark));
                builder.addCulledFace(Direction.SOUTH, this.createQuad(
                        v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 2, minV,
                        v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 0, minV,
                        v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 0, maxV,
                        v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 2, maxV, bark));


                break;
            case Z:

                builder.addCulledFace(Direction.WEST, this.createQuad(
                        v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV,
                        v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                        v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                        v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV, bark));
                builder.addCulledFace(Direction.WEST, this.createQuad(
                        v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f), 2, minV,
                        v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f), 0, minV,
                        v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f), 0, maxV,
                        v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f), 2, maxV, bark));
                builder.addCulledFace(Direction.EAST, this.createQuad(
                        v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV,
                        v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                        v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                        v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV, bark));
                builder.addCulledFace(Direction.EAST, this.createQuad(
                        v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f), 2, maxV,
                        v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f), 0, maxV,
                        v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f), 0, minV,
                        v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f), 2, minV, bark));

                builder.addCulledFace(Direction.DOWN, this.createQuad(
                        v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 16, maxV,
                        v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 14, maxV,
                        v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 14, minV,
                        v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 16, minV, bark));
                builder.addCulledFace(Direction.DOWN, this.createQuad(
                        v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 2, maxV,
                        v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 0, maxV,
                        v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 0, minV,
                        v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 2, minV, bark));
                builder.addCulledFace(Direction.UP, this.createQuad(
                        v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 16, minV,
                        v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 14, minV,
                        v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 14, maxV,
                        v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 16, maxV, bark));
                builder.addCulledFace(Direction.UP, this.createQuad(
                        v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 2, minV,
                        v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 0, minV,
                        v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 0, maxV,
                        v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 2, maxV, bark));

                break;
        }

        return builder.build();
    }

    public IBakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {

        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face: Direction.values()) {
            BlockFaceUV uvface = new BlockFaceUV(new float[]{ 8 - radius, 8 - radius, 8 + radius, 8 + radius }, getFaceAngle(axis, face));
            mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
        }

        BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrideList.EMPTY).particle(icon);

        for(Map.Entry<Direction, BlockPartFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    public IBakedModel bakeCoreSpikes(int radius, TextureAtlasSprite bark) {
        float minV = 8 - radius;
        float maxV = 8 + radius;

        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrideList.EMPTY).particle(bark);

        // X
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posTo.x() / 16f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 16, minV,
                v(posTo.x() / 16f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 14, minV,
                v(posFrom.x() / 16f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 14, maxV,
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 16, maxV, bark));
        builder.addCulledFace(Direction.DOWN, this.createQuad(
                v(posTo.x() / 16f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f), 2, minV,
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 0, minV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f - 0.002f), 0, maxV,
                v(posFrom.x() / 16f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f - 0.002f),  2, maxV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 16, maxV,
                v(posFrom.x() / 16f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 14, maxV,
                v(posTo.x() / 16f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 14, minV,
                v(posTo.x() / 16f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 16, minV, bark));
        builder.addCulledFace(Direction.DOWN, this.createQuad(
                v(posFrom.x() / 16f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 2, maxV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 0, maxV,
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f + 0.002f), 0, minV,
                v(posTo.x() / 16f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f + 0.002f), 2, minV, bark));

        builder.addCulledFace(Direction.SOUTH, this.createQuad(
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f - 0.0625f), 14, minV,
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f + 0.0625f), 16, minV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f + 0.0625f), 16, maxV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posTo.z() / 16f - 0.0625f), 14, maxV, bark));
        builder.addCulledFace(Direction.NORTH, this.createQuad(
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f - 0.0625f), 0, minV,
                v(posTo.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f + 0.0625f), 2, minV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f + 0.0625f), 2, maxV,
                v(posFrom.x() / 16f, posFrom.y() / 16f - 0.002f, posFrom.z() / 16f - 0.0625f), 0, maxV, bark));
        builder.addCulledFace(Direction.SOUTH, this.createQuad(
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f - 0.0625f), 14, maxV,
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f + 0.0625f), 16, maxV,
                v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f + 0.0625f), 16, minV,
                v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posTo.z() / 16f - 0.0625f), 14, minV, bark));
        builder.addCulledFace(Direction.NORTH, this.createQuad(
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f - 0.0625f), 0, maxV,
                v(posFrom.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f + 0.0625f), 2, maxV,
                v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f + 0.0625f), 2, minV,
                v(posTo.x() / 16f, posTo.y() / 16f + 0.002f, posFrom.z() / 16f - 0.0625f),  0, minV, bark));

        // Y
        builder.addCulledFace(Direction.SOUTH, this.createQuad(
                v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posTo.z() / 16f + 0.0625f), 16, minV,
                v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posTo.z() / 16f - 0.0625f), 14, minV,
                v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posTo.z() / 16f - 0.0625f), 14, maxV,
                v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posTo.z() / 16f + 0.0625f), 16, maxV, bark));
        builder.addCulledFace(Direction.NORTH, this.createQuad(
                v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, minV,
                v(posFrom.x() / 16f - 0.001f, posTo.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, minV,
                v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, maxV,
                v(posFrom.x() / 16f - 0.001f, posFrom.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, maxV, bark));
        builder.addCulledFace(Direction.SOUTH, this.createQuad(
                v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posTo.z() / 16f + 0.0625f), 16, maxV,
                v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posTo.z() / 16f - 0.0625f), 14, maxV,
                v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posTo.z() / 16f - 0.0625f), 14, minV,
                v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posTo.z() / 16f + 0.0625f), 16, minV, bark));
        builder.addCulledFace(Direction.NORTH, this.createQuad(
                v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, maxV,
                v(posTo.x() / 16f + 0.001f, posFrom.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, maxV,
                v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posFrom.z() / 16f - 0.0625f), 0, minV,
                v(posTo.x() / 16f + 0.001f, posTo.y() / 16f, posFrom.z() / 16f + 0.0625f), 2, minV, bark));

        builder.addCulledFace(Direction.EAST, this.createQuad(
                v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 16, maxV,
                v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 14, maxV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 14, minV,
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 16, minV, bark));
        builder.addCulledFace(Direction.WEST, this.createQuad(
                v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 2, maxV,
                v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f, posFrom.z() / 16f - 0.001f), 0, maxV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 0, minV,
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f, posFrom.z() / 16f - 0.001f), 2, minV, bark));
        builder.addCulledFace(Direction.EAST, this.createQuad(
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 16, minV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 14, minV,
                v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 14, maxV,
                v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 16, maxV, bark));
        builder.addCulledFace(Direction.WEST, this.createQuad(
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 2, minV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f, posTo.z() / 16f + 0.001f), 0, minV,
                v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 0, maxV,
                v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f, posTo.z() / 16f + 0.001f), 2, maxV, bark));

        // Z
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV, bark));
        builder.addCulledFace(Direction.DOWN, this.createQuad(
                v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f), 2, minV,
                v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f), 0, minV,
                v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f), 0, maxV,
                v(posFrom.x() / 16f - 0.002f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f), 2, maxV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV, bark));
        builder.addCulledFace(Direction.DOWN, this.createQuad(
                v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f + 0.0625f, posTo.z() / 16f), 2, maxV,
                v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f - 0.0625f, posTo.z() / 16f), 0, maxV,
                v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f - 0.0625f, posFrom.z() / 16f), 0, minV,
                v(posTo.x() / 16f + 0.002f, posFrom.y() / 16f + 0.0625f, posFrom.z() / 16f), 2, minV, bark));

        builder.addCulledFace(Direction.EAST, this.createQuad(
                v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 16, maxV,
                v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 14, maxV,
                v(posTo.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 14, minV,
                v(posTo.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 16, minV, bark));
        builder.addCulledFace(Direction.WEST, this.createQuad(
                v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 2, maxV,
                v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posTo.z() / 16f), 0, maxV,
                v(posFrom.x() / 16f - 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 0, minV,
                v(posFrom.x() / 16f + 0.0625f, posFrom.y() / 16f - 0.001f, posFrom.z() / 16f), 2, minV, bark));
        builder.addCulledFace(Direction.EAST, this.createQuad(
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 16, minV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 14, minV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 14, maxV,
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 16, maxV, bark));
        builder.addCulledFace(Direction.WEST, this.createQuad(
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 2, minV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 0, minV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 0, maxV,
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 2, maxV, bark));


        return builder.build();
    }

    public IBakedModel bakeTopSleeveSpikes(TextureAtlasSprite bark) {
        float minV = 4;
        float maxV = 12;

        Vector3f posFrom = new Vector3f(4, 16, 4);
        Vector3f posTo = new Vector3f(12, 16, 12);

        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrideList.EMPTY).particle(bark);


        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posTo, posTo, posFrom, 0, + 0.0625f, - 0.002f), 16, minV,
                v(posTo, posTo, posFrom, 0, - 0.0625f, - 0.002f), 14, minV,
                v(posFrom, posTo, posFrom, 0, - 0.0625f, - 0.002f), 14, maxV,
                v(posFrom, posTo, posFrom, 0, + 0.0625f, - 0.002f), 16, maxV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom, posTo, posTo, 0, + 0.0625f, + 0.002f), 16, maxV,
                v(posFrom, posTo, posTo, 0, - 0.0625f, + 0.002f), 14, maxV,
                v(posTo, posTo, posTo, 0, - 0.0625f, + 0.002f), 14, minV,
                v(posTo, posTo, posTo, 0, + 0.0625f, + 0.002f), 16, minV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom, posTo, posTo, 0, + 0.002f, - 0.0625f), 14, maxV,
                v(posFrom, posTo, posTo, 0, + 0.002f, + 0.0625f), 16, maxV,
                v(posTo, posTo, posTo, 0, + 0.002f, + 0.0625f), 16, minV,
                v(posTo, posTo, posTo, 0, + 0.002f, - 0.0625f), 14, minV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom, posTo, posFrom, 0, + 0.002f, - 0.0625f), 0, maxV,
                v(posFrom, posTo, posFrom, 0, + 0.002f, + 0.0625f), 2, maxV,
                v(posTo, posTo, posFrom, 0, + 0.002f, + 0.0625f), 2, minV,
                v(posTo, posTo, posFrom, 0, + 0.002f, - 0.0625f), 0, minV, bark));

        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                v(posFrom.x() / 16f - 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posTo.z() / 16f), 16, maxV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posTo.z() / 16f), 14, maxV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f - 0.0625f, posFrom.z() / 16f), 14, minV,
                v(posTo.x() / 16f + 0.002f, posTo.y() / 16f + 0.0625f, posFrom.z() / 16f), 16, minV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 16, minV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 14, minV,
                v(posTo.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 14, maxV,
                v(posTo.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 16, maxV, bark));
        builder.addCulledFace(Direction.UP, this.createQuad(
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 2, minV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posFrom.z() / 16f), 0, minV,
                v(posFrom.x() / 16f - 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 0, maxV,
                v(posFrom.x() / 16f + 0.0625f, posTo.y() / 16f + 0.001f, posTo.z() / 16f), 2, maxV, bark));


        return builder.build();
    }

    /**
     * A Hack to determine the UV face angle for a block column on a certain axis
     *
     * @param axis
     * @param face
     * @return
     */
    public int getFaceAngle (Axis axis, Direction face) {
        if(axis == Axis.Y) { //UP / DOWN
            return 0;
        }
        else if(axis == Axis.Z) {//NORTH / SOUTH
            switch(face) {
                case UP: return 0;
                case WEST: return 270;
                case DOWN: return 180;
                default: return 90;
            }
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    private Vector3d v(Vector3f xVec, Vector3f yVec, Vector3f zVec, float xOffset, float yOffset, float zOffset){
        return v(xVec.x() / 16f + xOffset, yVec.y() / 16f + yOffset, zVec.z() / 16f + zOffset);
    }

    private Vector3d v(float x, float y, float z) {
        return new Vector3d(x, y, z);
    }

    private int getRadiusIndex (int radius){
        for (int i=0; i<radii.length; i++){
            if (radius == radii[i]) return i;
        }
        return 0;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {

        if (side == null && state != null) {
            List<BakedQuad> quadsList = new ArrayList<>(12);

            int coreRadius = this.getRadius(state);

            int[] connections = new int[] {0,0,0,0,0,0};
            Direction forceRingDir = null;
            if (extraData instanceof ModelConnections){
                connections = ((ModelConnections) extraData).getAllRadii();
                forceRingDir = ((ModelConnections) extraData).getRingOnly();
            }


                //Count number of connections
                int numConnections = 0;
                for(int i: connections) {
                    numConnections += (i != 0) ? 1: 0;
                }

                if (numConnections == 0 && forceRingDir != null){
                    quadsList.addAll(rings[getRadiusIndex(coreRadius)].getQuads(state, forceRingDir, rand, extraData));
                } else {
                    boolean extraUpSleeve = false;
                    if (coreRadius == radii[0] && numConnections == 1 && state.getValue(CactusBranchBlock.ORIGIN).getAxis().isHorizontal()) {
                        connections[1] = radii[0];
                        extraUpSleeve = true;
                    }

                    //The source direction is the biggest connection from one of the 6 directions
                    Direction sourceDir = getSourceDir(coreRadius, connections);
                    if(sourceDir == null) {
                        sourceDir = Direction.DOWN;
                    }
                    int coreDir = resolveCoreDir(sourceDir);

                    // This is for drawing the rings on a terminating branch
                    Direction coreRingDir = (numConnections == 1) ? sourceDir.getOpposite() : null;

                    for (Direction face : Direction.values()) {
                        //Get quads for core model
                        if (coreRadius != connections[face.get3DDataValue()]) {
                            if (coreRingDir == null || coreRingDir != face) {
                                quadsList.addAll(cores[coreDir][getRadiusIndex(coreRadius)].getQuads(state, face, rand, extraData));
                            } else {
                                quadsList.addAll(rings[getRadiusIndex(coreRadius)].getQuads(state, face, rand, extraData));
                            }
                        }

                        // Get quads for core spikes
                        for (Direction dir : Direction.values()) {
                            if (coreRadius > connections[dir.get3DDataValue()]) {
                                for (BakedQuad quad : coreSpikes[getRadiusIndex(coreRadius)].getQuads(state, dir, rand, extraData)) {
                                    if (coreRadius > connections[quad.getDirection().get3DDataValue()]) {
                                        quadsList.add(quad);
                                    }
                                }
                            }
                        }

                        // Get quads for sleeves models
                        for (Direction connDir : Direction.values()) {
                            int idx = connDir.get3DDataValue();
                            int connRadius = connections[idx];
                            // If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
                            if (connRadius >= radii[0] && ((connDir == Direction.UP && connRadius == radii[0] && extraUpSleeve) || face != connDir || connDir == Direction.DOWN)) {
                                quadsList.addAll(sleeves[idx][getRadiusIndex(connRadius)].getQuads(state, face, rand, extraData));
                            }
                        }
                    }

                    if (extraUpSleeve) {
                        quadsList.addAll(sleeveTopSpikes.getQuads(state, Direction.UP, rand, extraData));
                    }
                }

            return quadsList;
        }

        return Collections.emptyList();
    }

    /**
     * Checks all neighboring tree parts to determine the connection radius for each side of this branch block.
     *
     */
    @Override
    public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
        Block block = state.getBlock();
        return block instanceof BranchBlock ? new ModelConnections(((BranchBlock) block).getConnectionData(world, pos, state)) : new ModelConnections();
    }

    /**
     * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
     *
     * @param coreRadius
     * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
     * @return
     */
    @Nullable
    protected Direction getSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for(Direction dir: Direction.values()){
            int connRadius = connections[dir.get3DDataValue()];
            if(connRadius > largestConnection){
                largestConnection = connRadius;
                sourceDir = dir;
            }
        }

        if(largestConnection < coreRadius){
            sourceDir = null;//Has no source node
        }
        return sourceDir;
    }

    /**
     * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
     *
     * @param dir
     * @return
     */
    protected int resolveCoreDir(Direction dir) {
        return dir.get3DDataValue() >> 1;
    }

    protected int getRadius(BlockState blockState) {
        // This way works with branches that don't have the RADIUS property, like cactus
        return ((CactusBranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IModelData data) {
        return getParticleIcon();
    }
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return barkTexture;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

}
