package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;

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
                final RenderType renderType = box.clipped() ? WorldRenderMacros.LINES_WITH_WIDTH : WorldRenderMacros.GLINT_LINES_WITH_WIDTH;
                final VertexConsumer buffer = ctx.bufferSource.getBuffer(renderType);

                final float halfLine = box.width() / 2.0f;
                final float minX = (float) (box.bounds().minX - halfLine);
                final float minY = (float) (box.bounds().minY - halfLine);
                final float minZ = (float) (box.bounds().minZ - halfLine);
                final float minX2 = minX + box.width();
                final float minY2 = minY + box.width();
                final float minZ2 = minZ + box.width();

                final float maxX = (float) (box.bounds().maxX + halfLine);
                final float maxY = (float) (box.bounds().maxY + halfLine);
                final float maxZ = (float) (box.bounds().maxZ + halfLine);
                final float maxX2 = maxX - box.width();
                final float maxY2 = maxY - box.width();
                final float maxZ2 = maxZ - box.width();

                buffer.defaultColor(FastColor.ARGB32.red(box.color()), FastColor.ARGB32.green(box.color()), FastColor.ARGB32.blue(box.color()), FastColor.ARGB32.alpha(box.color()));
                WorldRenderMacros.populateRenderLineBox(minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2, ctx.poseStack.last().pose(), buffer);
                buffer.unsetDefaultColor();
            }
        }
    }
}
