package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/**
 * Renders boxes for {@link com.minecolonies.api.items.IBlockOverlayItem}
 */
public class ItemOverlayBoxesRenderer
{
    /**
     * Renders overlay boxes into the client.
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() instanceof final IBlockOverlayItem overlayItem)
        {
            final List<IBlockOverlayItem.OverlayBox> boxes = overlayItem.getOverlayBoxes(ctx.clientLevel, ctx.clientPlayer, ctx.mainHandItem);

            for (final IBlockOverlayItem.OverlayBox box : boxes)
            {
                ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource, box.bounds(), box.width(), box.color(), box.showThroughBlocks());
            }

            ColonyWorldRenderMacros.endRenderLineBox(ctx.bufferSource);
        }
    }

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
