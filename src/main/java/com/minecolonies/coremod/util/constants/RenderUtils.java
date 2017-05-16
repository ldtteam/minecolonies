package com.minecolonies.coremod.util.constants;

import com.minecolonies.blockout.Render;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * Used for some rendering purpose like waypoint rendering.
 */
public final class RenderUtils
{
    /**
     * Half point offset to get in the middle of a block.
     */
    private static final double HALF_BLOCK_OFFSET = 0.5;

    /**
     * Offset where the waypoint rendering marks to.
     */
    private static final int FIX_POINT_OFFSET = 10;

    /**
     * Private constructor to hide the explicit one.
     */
    private RenderUtils()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Render all waypoints.
     * @param position the position of the build tool click.
     * @param theWorld the world.
     * @param partialTicks the partial ticks
     */
    public static void renderWayPoints(final BlockPos position, final WorldClient clientWorld, final float partialTicks)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(clientWorld, position);

        if(colonyView == null)
        {
            return;
        }

        final Set<BlockPos> waypoints = colonyView.getWayPoints();
        for (final BlockPos pos : waypoints)
        {
            final EntityEnderCrystal crystal = new EntityEnderCrystal(clientWorld);
            crystal.setPosition(pos.getX() + HALF_BLOCK_OFFSET, pos.getY(), pos.getZ() + HALF_BLOCK_OFFSET);
            crystal.setBeamTarget(pos.up(FIX_POINT_OFFSET));
            crystal.setShowBottom(false);
            crystal.innerRotation = 0;

            Minecraft.getMinecraft().getRenderManager().renderEntityStatic(crystal, 0.0F, true);
        }
    }
}
