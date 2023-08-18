package com.minecolonies.coremod.client.render.worldevent;

import com.minecolonies.api.items.IBlockOverlayItem;

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
        if (ctx.getMainHandItem().getItem() instanceof final IBlockOverlayItem overlayItem)
        {
            final List<IBlockOverlayItem.OverlayBox> boxes = overlayItem.getOverlayBoxes(ctx.getClientLevel(), ctx.getClientPlayer(), ctx.getMainHandItem());

            for (final IBlockOverlayItem.OverlayBox box : boxes)
            {
                ColonyWorldRenderMacros.renderLineBox(ctx.getPoseStack(), ctx.getBufferSource(), box.bounds(), box.width(), box.color(), box.showThroughBlocks());
            }

            ColonyWorldRenderMacros.endRenderLineBox(ctx.getBufferSource());
        }
    }
}
