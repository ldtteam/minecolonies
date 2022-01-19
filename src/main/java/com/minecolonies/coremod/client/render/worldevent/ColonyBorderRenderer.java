package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.blockui.MatrixUtils;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.util.MutableChunkPos;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

public class ColonyBorderRenderer
{
    private static final int RENDER_DIST_THRESHOLD = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int PLAYER_CHUNK_STEP = CHUNK_SIZE / 4;

    private static Pair<DrawState, ByteBuffer> colonies = null;
    private static Pair<DrawState, ByteBuffer> chunktickets = null;
    private static ChunkPos lastPlayerChunkPos = null;
    private static IColonyView lastColony = null;

    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.buildTool.get() || !ctx.hasNearestColony())
        {
            return;
        }

        final ChunkPos playerChunkPos = new ChunkPos(ctx.clientPlayer.blockPosition());

        if (lastColony != ctx.nearestColony || !lastPlayerChunkPos.equals(playerChunkPos))
        {
            lastColony = ctx.nearestColony;
            lastPlayerChunkPos = playerChunkPos;

            final Map<ChunkPos, Integer> coloniesMap = new HashMap<>();
            final Map<ChunkPos, Integer> chunkticketsMap = new HashMap<>();
            final BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            final int nearestColonyId = ctx.nearestColony.getID();
            final int playerRenderDist = Math.max(ctx.clientRenderDist - RENDER_DIST_THRESHOLD, 2);
            final int range = Math.max(ctx.clientRenderDist, MineColonies.getConfig().getServer().maxColonySize.get());

            for (int chunkX = -range; chunkX <= range; chunkX++)
            {
                for (int chunkZ = -range; chunkZ <= range; chunkZ++)
                {
                    final LevelChunk chunk = ctx.clientLevel.getChunk(playerChunkPos.x + chunkX, playerChunkPos.z + chunkZ);
                    final ChunkPos chunkPos = chunk.getPos();

                    chunk.getCapability(CLOSE_COLONY_CAP, null).ifPresent(cap -> coloniesMap.put(chunkPos, cap.getOwningColony()));
                    if (ctx.nearestColony.getTicketedChunks().contains(chunkPos.toLong()))
                    {
                        chunkticketsMap.put(chunkPos, nearestColonyId);
                    }
                    else
                    {
                        chunkticketsMap.put(chunkPos, 0);
                    }
                }
            }

            colonies = draw(bufferbuilder, coloniesMap, nearestColonyId, playerChunkPos, playerRenderDist);
            chunktickets = draw(bufferbuilder, chunkticketsMap, nearestColonyId, playerChunkPos, playerRenderDist);
            bufferbuilder.unsetDefaultColor();
        }

        final Pair<DrawState, ByteBuffer> p = Screen.hasControlDown() ? chunktickets : colonies;
        final DrawState ds = p.getFirst();

        MatrixUtils.pushShaderMVstack(ctx.poseStack);
        WorldRenderMacros.LINES.setupRenderState();
        BufferUploader._end(p.getSecond(), ds.mode(), ds.format(), ds.vertexCount(), ds.indexType(), ds.indexCount(), ds.sequentialIndex());
        WorldRenderMacros.LINES.clearRenderState();
        MatrixUtils.popShaderMVstack();
    }

    private static Pair<DrawState, ByteBuffer> draw(final BufferBuilder bufferbuilder,
        final Map<ChunkPos, Integer> mapToDraw,
        final int playerColonyId,
        final ChunkPos playerChunkPos,
        final int playerRenderDist)
    {
        final MutableChunkPos mutableChunkPos = new MutableChunkPos(0, 0);
        final Map<Integer, Color> colonyColours = new HashMap<>();
        final boolean useColonyColour = IMinecoloniesAPI.getInstance().getConfig().getClient().colonyteamborders.get();

        bufferbuilder.begin(WorldRenderMacros.LINES.mode(), WorldRenderMacros.LINES.format());
        mapToDraw.forEach((chunkPos, colonyId) -> {
            if (colonyId == 0 || chunkPos.x <= playerChunkPos.x - playerRenderDist || chunkPos.x >= playerChunkPos.x + playerRenderDist
                || chunkPos.z <= playerChunkPos.z - playerRenderDist || chunkPos.z >= playerChunkPos.z + playerRenderDist)
            {
                return;
            }

            final boolean isPlayerChunkX = colonyId == playerColonyId && chunkPos.x == playerChunkPos.x;
            final boolean isPlayerChunkZ = colonyId == playerColonyId && chunkPos.z == playerChunkPos.z;
            final float minX = chunkPos.getMinBlockX();
            final float maxX = chunkPos.getMaxBlockX() + 1.0f;
            final float minZ = chunkPos.getMinBlockZ();
            final float maxZ = chunkPos.getMaxBlockZ() + 1.0f;
            final int testedColonyId = colonyId;

            if (useColonyColour)
            {
                final Color colour = colonyColours.computeIfAbsent(colonyId, id ->
                {
                    final IColonyView colony = IMinecoloniesAPI.getInstance().getColonyManager().getColonyView(id, Minecraft.getInstance().level.dimension());
                    final ChatFormatting team = colony != null ? colony.getTeamColonyColor()
                            : id == playerColonyId ? ChatFormatting.WHITE : ChatFormatting.RED;
                    return new Color(team.getColor());
                });

                bufferbuilder.defaultColor(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha());
            }
            else if (colonyId == playerColonyId)
            {
                bufferbuilder.defaultColor(255, 255, 255, 255);
            }
            else
            {
                bufferbuilder.defaultColor(255, 70, 70, 255);
            }

            mutableChunkPos.setX(chunkPos.x);
            mutableChunkPos.setZ(chunkPos.z - 1);
            final boolean north = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setZ(chunkPos.z + 1);
            final boolean south = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x + 1);
            mutableChunkPos.setZ(chunkPos.z);
            final boolean east = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x - 1);
            final boolean west = mapToDraw.getOrDefault(mutableChunkPos, -1) != testedColonyId;

            // vert lines
            if (north || west)
            {
                bufferbuilder.vertex(minX, 0, minZ).endVertex();
                bufferbuilder.vertex(minX, CHUNK_HEIGHT, minZ).endVertex();
            }
            if (north || east)
            {
                bufferbuilder.vertex(maxX, 0, minZ).endVertex();
                bufferbuilder.vertex(maxX, CHUNK_HEIGHT, minZ).endVertex();
            }
            if (south || west)
            {
                bufferbuilder.vertex(minX, 0, maxZ).endVertex();
                bufferbuilder.vertex(minX, CHUNK_HEIGHT, maxZ).endVertex();
            }
            if (south || east)
            {
                bufferbuilder.vertex(maxX, 0, maxZ).endVertex();
                bufferbuilder.vertex(maxX, CHUNK_HEIGHT, maxZ).endVertex();
            }

            // horizontal lines
            if (north)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX + shift, 0, minZ).endVertex();
                        bufferbuilder.vertex(minX + shift, CHUNK_HEIGHT, minZ).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, minZ).endVertex();
                        bufferbuilder.vertex(maxX, y, minZ).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, minZ).endVertex();
                        bufferbuilder.vertex(maxX, y, minZ).endVertex();
                    }
                }
            }
            if (south)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX + shift, 0, maxZ).endVertex();
                        bufferbuilder.vertex(minX + shift, CHUNK_HEIGHT, maxZ).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, maxZ).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, maxZ).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).endVertex();
                    }
                }
            }
            if (west)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, 0, minZ + shift).endVertex();
                        bufferbuilder.vertex(minX, CHUNK_HEIGHT, minZ + shift).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, minZ).endVertex();
                        bufferbuilder.vertex(minX, y, maxZ).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, minZ).endVertex();
                        bufferbuilder.vertex(minX, y, maxZ).endVertex();
                    }
                }
            }
            if (east)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(maxX, 0, minZ + shift).endVertex();
                        bufferbuilder.vertex(maxX, CHUNK_HEIGHT, minZ + shift).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(maxX, y, minZ).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(maxX, y, minZ).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).endVertex();
                    }
                }
            }
        });

        bufferbuilder.end();

        // create bytebuffer copy since buffer builder uses slice
        final Pair<DrawState, ByteBuffer> preResult = bufferbuilder.popNextBuffer();
        final ByteBuffer temp = MemoryTracker.create(preResult.getSecond().capacity());
        preResult.getSecond().clear();
        temp.clear();
        temp.order(preResult.getSecond().order()); // FORGE: Fix incorrect byte order
        temp.put(preResult.getSecond());
        return Pair.of(preResult.getFirst(), temp);
    }
}
