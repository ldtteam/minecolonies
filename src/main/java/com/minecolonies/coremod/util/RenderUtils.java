package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import com.minecolonies.structures.lib.ModelHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
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
     * Degrees of a whole circle.
     */
    private static final int WHOLE_CIRCLE  = 360;

    /**
     * Half a circle radius.
     */
    private static final int HALF_A_CIRCLE = 180;

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
     * @param clientWorld the world.
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

    /**
     * Render the colony border.
     * @param position the position of the structure.
     * @param clientWorld the world.
     * @param partialTicks the partial ticks.
     * @param thePlayer the player clicking.
     * @param colonyBorder the border of the colony.
     */
    public static void renderColonyBorder(
            final BlockPos position,
            final WorldClient clientWorld,
            final float partialTicks,
            final EntityPlayerSP thePlayer,
            final List<BlockPos> colonyBorder)
    {
        if(colonyBorder.isEmpty())
        {
            calculateColonyBorder(clientWorld, thePlayer, colonyBorder);
        }

        for (final BlockPos pos : colonyBorder)
        {
            final Block block = Blocks.DIAMOND_BLOCK;
            final IBlockState iblockstate = block.getDefaultState();
            final IBlockState iBlockExtendedState = block.getExtendedState(iblockstate, clientWorld, pos);
            final IBakedModel ibakedmodel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(iblockstate);
            final TileEntity tileentity = null;

            final ModelHolder models = new ModelHolder(pos, iblockstate, iBlockExtendedState, tileentity, ibakedmodel);
            Structure.getQuads(models, models.quads);
            Settings.instance.getActiveStructure().renderGhost(clientWorld, models, thePlayer, partialTicks);
        }
    }

    /**
     * Calculate the colony border.
     * @param theWorld in the world.
     * @param thePlayer with the player.
     * @param colonyBorder the border.
     */
    private static void calculateColonyBorder(final WorldClient theWorld, final EntityPlayerSP thePlayer, final List<BlockPos> colonyBorder)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(theWorld, thePlayer.getPosition());
        if(colonyView == null)
        {
            return;
        }
        final BlockPos center = colonyView.getCenter();
        final int radius = Configurations.gameplay.workingRangeTownHall;

        for ( double degrees = 0; degrees < WHOLE_CIRCLE; degrees += 1 )
        {
            final double rads = degrees / HALF_A_CIRCLE * Math.PI;
            final double x = Math.round( center.getX( ) + radius * Math.sin( rads ) );
            final double z = Math.round( center.getZ( ) + radius * Math.cos( rads ) );
            colonyBorder.add(BlockPosUtil.getFloor(new BlockPos(x, center.getY(), z), theWorld).up());
        }
    }
}
