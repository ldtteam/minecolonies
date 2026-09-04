package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.blockui.util.color.ColourARGB;
import com.ldtteam.blockui.util.color.ColourQuartet4i;
import com.ldtteam.blockui.util.color.IColour;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.core.MineColonies;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class ColonyBorderRenderer
{
    private static final int RENDER_DIST_THRESHOLD = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int PLAYER_CHUNK_STEP = CHUNK_SIZE / 4;

    private static ChunkPos lastPlayerChunkPos = null;
    private static IColonyView lastColony = null;
    private static boolean lastShowTickets = false;

    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.buildTool.get() || !ctx.hasNearestColony())
        {
            return;
        }

        final var blockPosition = ctx.clientPlayer.blockPosition();
        final ChunkPos playerChunkPos = new ChunkPos(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        final boolean showTickets = Minecraft.getInstance().hasControlDown();
        if (lastColony != ctx.nearestColony
              || lastShowTickets != showTickets
              || !playerChunkPos.equals(lastPlayerChunkPos))
        {
            lastColony = ctx.nearestColony;
            lastPlayerChunkPos = playerChunkPos;
            lastShowTickets = showTickets;
        }

        final Map<ChunkPos, Integer> chunksToDraw = new HashMap<>();
        final int nearestColonyId = ctx.nearestColony.getID();
        final int playerRenderDist = Math.max(ctx.clientRenderDist - RENDER_DIST_THRESHOLD, 2);
        final int range = Math.max(ctx.clientRenderDist, MineColonies.getConfig().getServer().maxColonySize.get());

        for (int chunkX = -range; chunkX <= range; chunkX++)
        {
            for (int chunkZ = -range; chunkZ <= range; chunkZ++)
            {
                final LevelChunk chunk = ctx.clientLevel.getChunk(playerChunkPos.x() + chunkX, playerChunkPos.z() + chunkZ);
                if (chunk.isEmpty())
                {
                    continue;
                }

                final ChunkPos chunkPos = chunk.getPos();
                final IChunkClaimData claimData =
                    IColonyManager.getInstance().getClaimData(ctx.nearestColony.getDimension(), chunkPos);
                if (!showTickets && claimData != null && claimData.getOwningColony() != 0)
                {
                    chunksToDraw.put(chunkPos, claimData.getOwningColony());
                }
                else if (showTickets && ctx.nearestColony.getTicketedChunks().contains(chunkPos.pack()))
                {
                    chunksToDraw.put(chunkPos, nearestColonyId);
                }
            }
        }

        draw(ctx, chunksToDraw, nearestColonyId, playerChunkPos, playerRenderDist);
    }

    private static void draw(final WorldEventContext ctx,
        final Map<ChunkPos, Integer> mapToDraw,
        final int playerColonyId,
        final ChunkPos playerChunkPos,
        final int playerRenderDist)
    {
        final Map<Integer, IColour> colonyColours = new HashMap<>();
        final boolean useColonyColour = IMinecoloniesAPI.getInstance().getConfig().getClient().colonyteamborders.get();
        final VertexConsumer buffer = ctx.bufferSource.getBuffer(WorldRenderMacros.LINES);

        mapToDraw.forEach((chunkPos, colonyId) -> {
            if (colonyId == 0 || chunkPos.x() <= playerChunkPos.x() - playerRenderDist
                  || chunkPos.x() >= playerChunkPos.x() + playerRenderDist
                  || chunkPos.z() <= playerChunkPos.z() - playerRenderDist
                  || chunkPos.z() >= playerChunkPos.z() + playerRenderDist)
            {
                return;
            }

            final boolean isPlayerChunkX = colonyId == playerColonyId && chunkPos.x() == playerChunkPos.x();
            final boolean isPlayerChunkZ = colonyId == playerColonyId && chunkPos.z() == playerChunkPos.z();
            final int minX = chunkPos.getMinBlockX();
            final int maxX = chunkPos.getMaxBlockX() + 1;
            final int minZ = chunkPos.getMinBlockZ();
            final int maxZ = chunkPos.getMaxBlockZ() + 1;
            final int minY = ctx.clientLevel.getMinY();
            final int maxY = ctx.clientLevel.getMaxY();
            final int color = getColor(colonyColours, colonyId, playerColonyId, useColonyColour, ctx);

            final boolean north = mapToDraw.getOrDefault(new ChunkPos(chunkPos.x(), chunkPos.z() - 1), -1) != colonyId;
            final boolean south = mapToDraw.getOrDefault(new ChunkPos(chunkPos.x(), chunkPos.z() + 1), -1) != colonyId;
            final boolean east = mapToDraw.getOrDefault(new ChunkPos(chunkPos.x() + 1, chunkPos.z()), -1) != colonyId;
            final boolean west = mapToDraw.getOrDefault(new ChunkPos(chunkPos.x() - 1, chunkPos.z()), -1) != colonyId;

            addLine(buffer, minX, minY, minZ, minX, maxY, minZ, north || west, color);
            addLine(buffer, maxX, minY, minZ, maxX, maxY, minZ, north || east, color);
            addLine(buffer, minX, minY, maxZ, minX, maxY, maxZ, south || west, color);
            addLine(buffer, maxX, minY, maxZ, maxX, maxY, maxZ, south || east, color);

            if (north)
            {
                addChunkTicks(buffer, isPlayerChunkZ, minX, maxX, minY, maxZ, minZ, true, color);
            }
            if (south)
            {
                addChunkTicks(buffer, isPlayerChunkZ, minX, maxX, minY, maxZ, maxZ, true, color);
            }
            if (west)
            {
                addChunkTicks(buffer, isPlayerChunkX, minZ, maxZ, minY, maxZ, minX, false, color);
            }
            if (east)
            {
                addChunkTicks(buffer, isPlayerChunkX, minZ, maxZ, minY, maxZ, maxX, false, color);
            }
        });
    }

    private static int getColor(final Map<Integer, IColour> colors,
        final int colonyId,
        final int playerColonyId,
        final boolean useColonyColour,
        final WorldEventContext ctx)
    {
        if (!useColonyColour)
        {
            return colonyId == playerColonyId ? new ColourQuartet4i(255, 255, 255, 255).argb()
                                              : new ColourQuartet4i(70, 70, 255, 255).argb();
        }

        return colors.computeIfAbsent(colonyId, id -> {
            final IColonyView colony =
                IMinecoloniesAPI.getInstance().getColonyManager().getColonyView(id, ctx.clientLevel.dimension());
            final ChatFormatting team = colony != null ? colony.getTeamColonyColor()
                : id == playerColonyId ? ChatFormatting.WHITE : ChatFormatting.RED;
            final int rgb = net.minecraft.network.chat.TextColor.fromLegacyFormat(team).getValue();
            return new ColourARGB(rgb | 0xff000000);
        }).argb();
    }

    private static void addLine(final VertexConsumer buffer,
        final int x1,
        final int y1,
        final int z1,
        final int x2,
        final int y2,
        final int z2,
        final boolean visible,
        final int color)
    {
        if (!visible)
        {
            return;
        }
        buffer.addVertex(x1, y1, z1).setColor(color);
        buffer.addVertex(x2, y2, z2).setColor(color);
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    private static void addChunkTicks(final VertexConsumer buffer,
        final boolean dense,
        final int from1,
        final int to1,
        final int minY,
        final int maxY,
        final int fixed,
        final boolean fixedIsZ,
        final int color)
    {
        final int step = dense ? PLAYER_CHUNK_STEP : CHUNK_SIZE;
        for (int value = from1 + step; value < to1; value += step)
        {
            if (fixedIsZ)
            {
                addLine(buffer, value, minY, fixed, value, maxY, fixed, true, color);
            }
            else
            {
                addLine(buffer, fixed, minY, value, fixed, maxY, value, true, color);
            }
        }
    }

    public static void cleanup()
    {
        lastColony = null;
        lastPlayerChunkPos = null;
        lastShowTickets = false;
    }
}
