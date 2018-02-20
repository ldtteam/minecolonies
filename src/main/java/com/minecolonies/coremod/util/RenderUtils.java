package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.blocks.BlockInfoPoster;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import com.minecolonies.structures.lib.ModelHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;

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
     *
     * @param position     the position of the build tool click.
     * @param clientWorld  the world.
     * @param partialTicks the partial ticks
     */
    public static void renderWayPoints(final BlockPos position, final WorldClient clientWorld, final float partialTicks)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(clientWorld, position);

        if (colonyView == null)
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
     * Render informal signs at the citizen.
     *
     * @param clientWorld     the client world.
     * @param partialTicks    the partial ticks.
     * @param citizenDataView the citizen data.
     * @param player          the player.
     * @param citizen         the citizen position
     */
    public static void renderSigns(
                                    final WorldClient clientWorld,
                                    final float partialTicks,
                                    final CitizenDataView citizenDataView,
                                    final EntityPlayer player,
                                    final BlockPos citizen)
    {
        final Block block = ModBlocks.blockInfoPoster;
        final BlockPos vector = citizen.subtract(player.getPosition());
        final EnumFacing facing = EnumFacing.getFacingFromVector(vector.getX(), 0, vector.getZ()).getOpposite();
        final BlockPos pos = citizen.up(2).offset(facing);

        final IBlockState iblockstate = block.getDefaultState().withProperty(BlockInfoPoster.FACING, facing);
        final IBlockState iBlockExtendedState = block.getExtendedState(iblockstate, clientWorld, pos);
        final IBakedModel ibakedmodel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(iblockstate);
        final TileEntityInfoPoster sign = new TileEntityInfoPoster();
        sign.setPos(pos);

        for (int i = 0; i < sign.signText.length; i++)
        {
            if (i < citizenDataView.getLatestStatus().length)
            {
                sign.signText[i] = citizenDataView.getLatestStatus()[i];
            }
        }

        final ModelHolder models = new ModelHolder(pos, iblockstate, iBlockExtendedState, sign, ibakedmodel);
        Structure.getQuads(models, models.quads);

        new Structure(Minecraft.getMinecraft().world).renderGhost(clientWorld, models, player, partialTicks, false);
    }

    /**
     * Render the colony border.
     *
     * @param position     the position of the structure.
     * @param clientWorld  the world.
     * @param partialTicks the partial ticks.
     * @param thePlayer    the player clicking.
     * @param colonyBorder the border of the colony.
     */
    public static void renderColonyBorder(
                                           final BlockPos position,
                                           final WorldClient clientWorld,
                                           final float partialTicks,
                                           final EntityPlayer thePlayer,
                                           final List<BlockPos> colonyBorder)
    {
        if (colonyBorder.isEmpty())
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
            Settings.instance.getActiveStructure().renderGhost(clientWorld, models, thePlayer, partialTicks, false);
        }
    }

    /**
     * Calculate the colony border.
     *
     * @param theWorld     in the world.
     * @param thePlayer    with the player.
     * @param colonyBorder the border.
     */
    private static void calculateColonyBorder(final WorldClient theWorld, final EntityPlayer thePlayer, final List<BlockPos> colonyBorder)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(theWorld, thePlayer.getPosition());
        if (colonyView == null)
        {
            return;
        }
        final int distance = BLOCKS_PER_CHUNK * Configurations.gameplay.workingRangeTownHallChunks;
        final BlockPos center = colonyView.getCenter();
        final Chunk chunk = theWorld.getChunkFromBlockCoords(center);
        int x = chunk.x;
        int z = chunk.z;

        int lowerEndX = x * BLOCKS_PER_CHUNK;
        int lowerEndZ = z * BLOCKS_PER_CHUNK;

        final int edgeX = lowerEndX - distance;
        final int edgeZ = lowerEndZ - distance;

        for(int i = 0; i <= distance * 2; i++)
        {
            colonyBorder.add(BlockPosUtil.getFloor(new BlockPos(edgeX + i, center.getY(), lowerEndZ + BLOCKS_PER_CHUNK + distance), theWorld).up());
            colonyBorder.add(BlockPosUtil.getFloor(new BlockPos(edgeX + i, center.getY(), lowerEndZ - distance), theWorld).up());
            colonyBorder.add(BlockPosUtil.getFloor(new BlockPos(lowerEndX - distance, center.getY(), edgeZ + i), theWorld).up());
            colonyBorder.add(BlockPosUtil.getFloor(new BlockPos(lowerEndX + BLOCKS_PER_CHUNK + distance, center.getY(), edgeZ + i), theWorld).up());
        }
    }
}
