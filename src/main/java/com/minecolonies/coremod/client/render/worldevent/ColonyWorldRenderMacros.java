package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;

/**
 * Extra {@link com.ldtteam.structurize.util.WorldRenderMacros}.  Maybe port it to Structurize at some point.
 */
public class ColonyWorldRenderMacros
{
    /**
     * Render a wireframe box.
     * @param poseStack         pose stack
     * @param bufferSource      buffer source
     * @param bounds            bounding box to draw
     * @param width             line width
     * @param color             line color (ARGB)
     * @param showThroughBlocks true to render through existing blocks, false to only render in air
     */
    public static void renderLineBox(final PoseStack poseStack, final MultiBufferSource.BufferSource bufferSource,
      final AABB bounds, final float width, final int color, final boolean showThroughBlocks)
    {
        final float halfLine = width / 2.0f;
        final float minX = (float) (bounds.minX - halfLine);
        final float minY = (float) (bounds.minY - halfLine);
        final float minZ = (float) (bounds.minZ - halfLine);
        final float minX2 = minX + width;
        final float minY2 = minY + width;
        final float minZ2 = minZ + width;

        final float maxX = (float) (bounds.maxX + halfLine);
        final float maxY = (float) (bounds.maxY + halfLine);
        final float maxZ = (float) (bounds.maxZ + halfLine);
        final float maxX2 = maxX - width;
        final float maxY2 = maxY - width;
        final float maxZ2 = maxZ - width;

        final int red = FastColor.ARGB32.red(color);
        final int green = FastColor.ARGB32.green(color);
        final int blue = FastColor.ARGB32.blue(color);
        final int alpha = FastColor.ARGB32.alpha(color);

        if (showThroughBlocks)
        {
            renderLineBox(poseStack, bufferSource.getBuffer(RenderTypes.LINES_INSIDE_BLOCKS),
              minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2,
              red / 2, green / 2, blue / 2, alpha / 2);
        }

        renderLineBox(poseStack, bufferSource.getBuffer(RenderTypes.LINES_OUTSIDE_BLOCKS),
          minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2,
          red, green, blue, alpha);
    }

    /**
     * Call after a series of {@link #renderLineBox(PoseStack, MultiBufferSource.BufferSource, AABB, float, int, boolean)}
     * @param bufferSource buffer source
     */
    public static void endRenderLineBox(final MultiBufferSource.BufferSource bufferSource)
    {
        bufferSource.endBatch(RenderTypes.LINES_INSIDE_BLOCKS);
        bufferSource.endBatch(RenderTypes.LINES_OUTSIDE_BLOCKS);
    }

    /**
     * Render a wireframe box.
     * @param poseStack pose stack
     * @param buffer    buffer
     * @param minX      min X
     * @param minY      min Y
     * @param minZ      min Z
     * @param minX2     min X + width
     * @param minY2     min Y + width
     * @param minZ2     min Z + width
     * @param maxX      max X
     * @param maxY      max Y
     * @param maxZ      max Z
     * @param maxX2     max X - width
     * @param maxY2     max Y - width
     * @param maxZ2     max Z - width
     * @param red       red
     * @param green     green
     * @param blue      blue
     * @param alpha     alpha
     */
    private static void renderLineBox(final PoseStack poseStack, final VertexConsumer buffer,
      final float minX, final float minY, final float minZ,
      final float minX2, final float minY2, final float minZ2,
      final float maxX, final float maxY, final float maxZ,
      final float maxX2, final float maxY2, final float maxZ2,
      final int red, final int green, final int blue, final int alpha)
    {
        buffer.defaultColor(red, green, blue, alpha);
        WorldRenderMacros.populateRenderLineBox(minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2, poseStack.last().pose(), buffer);
        buffer.unsetDefaultColor();
    }
}