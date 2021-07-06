package com.minecolonies.coremod.event;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.util.MutableChunkPos;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.BufferBuilder.DrawState;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class DebugRendererChunkBorder
{
    private static final double LINE_SHIFT = 0.003d;
    private static final int RENDER_DIST_THRESHOLD = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int PLAYER_CHUNK_STEP = CHUNK_SIZE / 4;

    private static Pair<DrawState, ByteBuffer> colonies = null;
    private static Pair<DrawState, ByteBuffer> chunktickets = null;
    private static ChunkPos lastPlayerChunk = null;
    private static IColonyView lastColonyView = null;

    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final PlayerEntity player = Minecraft.getInstance().player;

        if (player.getItemInHand(Hand.MAIN_HAND).getItem() != ModItems.buildTool.get())
        {
            return;
        }

        final World world = Minecraft.getInstance().level;
        final IColonyView nearestColonyView = IColonyManager.getInstance().getClosestColonyView(world, player.blockPosition());

        if (nearestColonyView == null)
        {
            return;
        }

        final ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
        final int playerRenderDist = Math.max(Minecraft.getInstance().options.renderDistance - RENDER_DIST_THRESHOLD, 2);

        if (lastColonyView != nearestColonyView || !lastPlayerChunk.equals(playerChunkPos))
        {
            lastColonyView = nearestColonyView;
            lastPlayerChunk = playerChunkPos;

            final Map<ChunkPos, Integer> coloniesMap = new HashMap<>();
            final Map<ChunkPos, Integer> chunkticketsMap = new HashMap<>();
            final int range = Math.max(Minecraft.getInstance().options.renderDistance,
                MineColonies.getConfig().getServer().maxColonySize.get());

            for (int chunkX = -range; chunkX <= range; chunkX++)
            {
                for (int chunkZ = -range; chunkZ <= range; chunkZ++)
                {
                    final Chunk chunk = world.getChunk(playerChunkPos.x + chunkX, playerChunkPos.z + chunkZ);
                    chunk.getCapability(CLOSE_COLONY_CAP, null).ifPresent(cap -> coloniesMap.put(chunk.getPos(), cap.getOwningColony()));

                    if (nearestColonyView.getTicketedChunks().contains(chunk.getPos().toLong()))
                    {
                        chunkticketsMap.put(chunk.getPos(), nearestColonyView.getID());
                    }
                    else
                    {
                        chunkticketsMap.put(chunk.getPos(), 0);
                    }
                }
            }

            final BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
            colonies = draw(bufferbuilder, coloniesMap, nearestColonyView.getID(), playerChunkPos, playerRenderDist);
            chunktickets = draw(bufferbuilder, chunkticketsMap, nearestColonyView.getID(), playerChunkPos, playerRenderDist);
        }

        final Vector3d currView = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        final MatrixStack stack = event.getMatrixStack();
        final Pair<DrawState, ByteBuffer> buffer = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),
            GLFW.GLFW_KEY_LEFT_CONTROL) ? chunktickets : colonies;

        stack.pushPose();
        stack.translate(-currView.x, -currView.y, -currView.z);

        RenderSystem.enableDepthTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);

        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(stack.last().pose());
        WorldVertexBufferUploader._end(buffer.getSecond(), buffer.getFirst().mode(), buffer.getFirst().format(), buffer.getFirst().vertexCount());
        RenderSystem.popMatrix();

        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);

        stack.popPose();
    }

    private static Pair<DrawState, ByteBuffer> draw(final BufferBuilder bufferbuilder,
        final Map<ChunkPos, Integer> mapToDraw,
        final int playerColonyId,
        final ChunkPos playerChunkPos,
        final int playerRenderDist)
    {
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        final MutableChunkPos mutableChunkPos = new MutableChunkPos(0, 0);

        mapToDraw.forEach((chunkPos, colonyId) -> {
            if (colonyId == 0 || chunkPos.x <= playerChunkPos.x - playerRenderDist || chunkPos.x >= playerChunkPos.x + playerRenderDist
                || chunkPos.z <= playerChunkPos.z - playerRenderDist || chunkPos.z >= playerChunkPos.z + playerRenderDist)
            {
                return;
            }

            final boolean isPlayerChunkX = colonyId == playerColonyId && chunkPos.x == playerChunkPos.x;
            final boolean isPlayerChunkZ = colonyId == playerColonyId && chunkPos.z == playerChunkPos.z;
            final double minX = chunkPos.getMinBlockX() + LINE_SHIFT;
            final double maxX = chunkPos.getMaxBlockX() + 1.0d - LINE_SHIFT;
            final double minZ = chunkPos.getMinBlockZ() + LINE_SHIFT;
            final double maxZ = chunkPos.getMaxBlockZ() + 1.0d - LINE_SHIFT;
            final int red;
            final int green;
            final int blue;
            final int alpha = 255;
            final int testedColonyId;

            if (colonyId == playerColonyId)
            {
                red = 255;
                green = 255;
                blue = 255;
                testedColonyId = playerColonyId;
            }
            else
            {
                red = 255;
                green = 70;
                blue = 70;
                testedColonyId = colonyId;
            }

            mutableChunkPos.setX(chunkPos.x);
            mutableChunkPos.setZ(chunkPos.z - 1);
            final boolean north = mapToDraw.containsKey(mutableChunkPos) && mapToDraw.get(mutableChunkPos) != testedColonyId;

            mutableChunkPos.setZ(chunkPos.z + 1);
            final boolean south = mapToDraw.containsKey(mutableChunkPos) && mapToDraw.get(mutableChunkPos) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x + 1);
            mutableChunkPos.setZ(chunkPos.z);
            final boolean east = mapToDraw.containsKey(mutableChunkPos) && mapToDraw.get(mutableChunkPos) != testedColonyId;

            mutableChunkPos.setX(chunkPos.x - 1);
            final boolean west = mapToDraw.containsKey(mutableChunkPos) && mapToDraw.get(mutableChunkPos) != testedColonyId;

            // vert lines
            if (north || west)
            {
                bufferbuilder.vertex(minX, 0, minZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(minX, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
            }
            if (north || east)
            {
                bufferbuilder.vertex(maxX, 0, minZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(maxX, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
            }
            if (south || west)
            {
                bufferbuilder.vertex(minX, 0, maxZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(minX, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
            }
            if (south || east)
            {
                bufferbuilder.vertex(maxX, 0, maxZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(maxX, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
            }

            // horizontal lines
            if (north)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX + shift, 0, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(minX + shift, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (south)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX + shift, 0, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(minX + shift, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (west)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, 0, minZ + shift).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(minX, CHUNK_HEIGHT, minZ + shift).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (east)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(maxX, 0, minZ + shift).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, CHUNK_HEIGHT, minZ + shift).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
        });

        bufferbuilder.end();

        // create bytebuffer copy since buffer builder uses slice
        final Pair<DrawState, ByteBuffer> preResult = bufferbuilder.popNextBuffer();
        ByteBuffer temp = GLAllocation.createByteBuffer(preResult.getSecond().capacity());
        ((Buffer) preResult.getSecond()).clear();
        ((Buffer) temp).clear();
        temp.put(preResult.getSecond());
        return Pair.of(preResult.getFirst(), temp);
    }
}
