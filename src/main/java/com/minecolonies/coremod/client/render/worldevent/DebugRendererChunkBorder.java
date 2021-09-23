package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.util.MutableChunkPos;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class DebugRendererChunkBorder
{
    public static void render(final WorldEventContext ctx)
    {
    }

    private static final double LINE_SHIFT = 0.003d;
    private static final int RENDER_DIST_THRESHOLD = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int PLAYER_CHUNK_STEP = CHUNK_SIZE / 4;

    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final Player player = Minecraft.getInstance().player;

        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != ModItems.buildTool.get())
        {
            return;
        }

        final Level world = Minecraft.getInstance().level;
        final IColonyView nearestColonyView = IColonyManager.getInstance().getClosestColonyView(world, player.blockPosition());

        if (nearestColonyView == null)
        {
            return;
        }

        final ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
        final int playerRenderDist = Math.max(Minecraft.getInstance().options.renderDistance - RENDER_DIST_THRESHOLD, 2);

        final Map<ChunkPos, Integer> coloniesMap = new HashMap<>();
        final Map<ChunkPos, Integer> chunkticketsMap = new HashMap<>();
        final int range = Math.max(Minecraft.getInstance().options.renderDistance,
            MineColonies.getConfig().getServer().maxColonySize.get());

        for (int chunkX = -range; chunkX <= range; chunkX++)
        {
            for (int chunkZ = -range; chunkZ <= range; chunkZ++)
            {
                final LevelChunk chunk = world.getChunk(playerChunkPos.x + chunkX, playerChunkPos.z + chunkZ);
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

        final Vec3 currView = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        final PoseStack stack = event.getMatrixStack();

        stack.pushPose();
        stack.translate(-currView.x, -currView.y, -currView.z);

        VertexConsumer buffer = null; // BORDER_LINE_RENDERER.get();

        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            draw(stack, buffer, chunkticketsMap, nearestColonyView.getID(), playerChunkPos, playerRenderDist);
        }
        else
        {
            draw(stack, buffer, coloniesMap, nearestColonyView.getID(), playerChunkPos, playerRenderDist);
        }

        stack.popPose();
    }

    private static void draw(final PoseStack stack,
        final VertexConsumer bufferbuilder,
        final Map<ChunkPos, Integer> mapToDraw,
        final int playerColonyId,
        final ChunkPos playerChunkPos,
        final int playerRenderDist)
    {
        final MutableChunkPos mutableChunkPos = new MutableChunkPos(0, 0);
        final Matrix4f matrix4f = stack.last().pose();

        mapToDraw.forEach((chunkPos, colonyId) -> {
            if (colonyId == 0 || chunkPos.x <= playerChunkPos.x - playerRenderDist || chunkPos.x >= playerChunkPos.x + playerRenderDist
                || chunkPos.z <= playerChunkPos.z - playerRenderDist || chunkPos.z >= playerChunkPos.z + playerRenderDist)
            {
                return;
            }

            // I'm unsure how we want to handle this, I don't remember the origin of this.
            final boolean isPlayerChunkX = true; // colonyId == playerColonyId && chunkPos.x == playerChunkPos.x;
            final boolean isPlayerChunkZ = true; // colonyId == playerColonyId && chunkPos.z == playerChunkPos.z;
            final float minX = (float) (chunkPos.getMinBlockX() + LINE_SHIFT);
            final float maxX = (float) (chunkPos.getMaxBlockX() + 1.0d - LINE_SHIFT);
            final float minZ = (float) (chunkPos.getMinBlockZ() + LINE_SHIFT);
            final float maxZ = (float) (chunkPos.getMaxBlockZ() + 1.0d - LINE_SHIFT);
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
                bufferbuilder.vertex(matrix4f, minX, 0, minZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix4f, minX, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
            }
            if (north || east)
            {
                bufferbuilder.vertex(matrix4f, maxX, 0, minZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix4f, maxX, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
            }
            if (south || west)
            {
                bufferbuilder.vertex(matrix4f, minX, 0, maxZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix4f, minX, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
            }
            if (south || east)
            {
                bufferbuilder.vertex(matrix4f, maxX, 0, maxZ).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix4f, maxX, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
            }

            // horizontal lines
            if (north)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX + shift, 0, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, minX + shift, CHUNK_HEIGHT, minZ).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (south)
            {
                if (isPlayerChunkX)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX + shift, 0, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, minX + shift, CHUNK_HEIGHT, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (west)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX, 0, minZ + shift).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, minX, CHUNK_HEIGHT, minZ + shift).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(matrix4f, minX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, minX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
            if (east)
            {
                if (isPlayerChunkZ)
                {
                    for (int shift = PLAYER_CHUNK_STEP; shift < CHUNK_SIZE; shift += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, maxX, 0, minZ + shift).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, CHUNK_HEIGHT, minZ + shift).color(red, green, blue, alpha).endVertex();
                    }
                    for (int y = PLAYER_CHUNK_STEP; y < CHUNK_HEIGHT; y += PLAYER_CHUNK_STEP)
                    {
                        bufferbuilder.vertex(matrix4f, maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
                else
                {
                    for (int y = CHUNK_SIZE; y < CHUNK_HEIGHT; y += CHUNK_SIZE)
                    {
                        bufferbuilder.vertex(matrix4f, maxX, y, minZ).color(red, green, blue, alpha).endVertex();
                        bufferbuilder.vertex(matrix4f, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
        });
    }
}
