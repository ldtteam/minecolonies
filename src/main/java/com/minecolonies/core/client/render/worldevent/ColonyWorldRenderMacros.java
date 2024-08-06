package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import static com.ldtteam.structurize.util.WorldRenderMacros.GLINT_LINES_WITH_WIDTH;

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
        WorldRenderMacros.renderLineBox(bufferSource.getBuffer(GLINT_LINES_WITH_WIDTH), poseStack, BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ), BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ), color, width);
    }

    /**
     * Call after a series of {@link #renderLineBox(PoseStack, MultiBufferSource.BufferSource, AABB, float, int, boolean)}
     * @param bufferSource buffer source
     */
    public static void endRenderLineBox(final MultiBufferSource.BufferSource bufferSource)
    {
        bufferSource.endBatch(GLINT_LINES_WITH_WIDTH);
    }
}
