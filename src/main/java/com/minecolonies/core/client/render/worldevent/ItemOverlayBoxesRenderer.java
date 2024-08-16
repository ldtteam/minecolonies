package com.minecolonies.core.client.render.worldevent;

import com.minecolonies.api.items.IBlockOverlayItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

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
                final BlockPos pos = box.pos();
                AABB bounds = box.bounds();
                if (pos == null)
                {
                    ctx.poseStack.pushPose();
                    ctx.poseStack.translate(bounds.minX - ctx.cameraPosition.x(), bounds.minY - ctx.cameraPosition.y(), bounds.minZ - ctx.cameraPosition.z());
                    bounds = bounds.move(-bounds.minX, -bounds.minY, -bounds.minZ);
                }
                else
                {
                    ctx.pushPoseCameraToPos(pos);
                    bounds = bounds.move(pos.multiply(-1));
                }

                if (box.showThroughBlocks())
                {
                    if (pos != null) ctx.renderLineBoxWithShadow(BlockPos.ZERO, box.color(), box.width());
                    if (bounds != null) ctx.renderLineAABBWithShadow(bounds, box.color(), box.width());
                }
                else
                {
                    if (pos != null) ctx.renderLineBox(WorldEventContext.LINES_WITH_WIDTH, BlockPos.ZERO, box.color(), box.width());
                    if (bounds != null) ctx.renderLineAABB(WorldEventContext.LINES_WITH_WIDTH, bounds, box.color(), box.width());
                }

                ctx.popPose();
            }
        }
    }
}
