package com.minecolonies.coremod.event;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.MineColonies;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class DebugRendererChunkBorder
{

    private static Tuple<Integer, Integer> center = new Tuple<>(0, 0);

    private static Map<Tuple<Integer, Integer>, Integer> colonies = new HashMap<>();

    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final double partialTicks = event.getPartialTicks();
        final PlayerEntity player = Minecraft.getInstance().player;

        if (player.getHeldItem(Hand.MAIN_HAND).getItem() != ModItems.buildTool)
        {
            return;
        }

        final World world = Minecraft.getInstance().world;
        final IColonyView view = IColonyManager.getInstance().getClosestColonyView(world, player.getPosition());

        if (view == null)
        {
            return;
        }

        if (!center.equals(new Tuple<>(player.chunkCoordX, player.chunkCoordZ)))
        {
            center = new Tuple<>(player.chunkCoordX, player.chunkCoordZ);
            colonies.clear();
            final int range = MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get();
            for (int incX = -range; incX <= range; incX += 1)
            {
                for (int incZ = -range; incZ <= range; incZ += 1)
                {
                    final Chunk chunk = world.getChunk(player.chunkCoordX + incX, player.chunkCoordZ + incZ);
                    final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (cap != null)
                    {
                        colonies.put(new Tuple<>(incX, incZ), cap.getOwningColony());
                    }
                }
            }
        }

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        final Vec3d currView = Minecraft.getInstance().getRenderManager().info.getProjectedView();
        final double lowerYLimit = 5 - currView.y;
        final double upperYLimit = 255 - currView.y;

        final double lowerYLimitSmaller = Math.max(lowerYLimit, currView.y - 30 - currView.y);

        GlStateManager.disableTexture();
        GlStateManager.disableBlend();

        final double chunkCoordX = ((double) (player.chunkCoordX << 4) - currView.x);
        final double chunkCoordZ = ((double) (player.chunkCoordZ << 4) - currView.z);

        GlStateManager.lineWidth(1.0F);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (final Map.Entry<Tuple<Integer, Integer>, Integer> c : colonies.entrySet())
        {
            final int x = c.getKey().getA();
            final int z = c.getKey().getB();
            final int incX = x * 16;
            final int incZ = z * 16;

            if (c.getValue() == view.getID())
            {
                boolean north = false;
                boolean south = false;
                boolean east = false;
                boolean west = false;

                if (!c.getValue().equals(colonies.get(new Tuple<>(x, z - 1))) && colonies.containsKey(new Tuple<>(x, z - 1)))
                {
                    north = true;
                }
                if (!c.getValue().equals(colonies.get(new Tuple<>(x, z + 1))) && colonies.containsKey(new Tuple<>(x, z + 1)))
                {
                    south = true;
                }
                if (!c.getValue().equals(colonies.get(new Tuple<>(x + 1, z))) && colonies.containsKey(new Tuple<>(x + 1, z)))
                {
                    east = true;
                }
                if (!c.getValue().equals(colonies.get(new Tuple<>(x - 1, z))) && colonies.containsKey(new Tuple<>(x - 1, z)))
                {
                    west = true;
                }

                double levels = lowerYLimit;
                while (levels <= upperYLimit)
                {
                    if (north)
                    {
                        bufferbuilder.vertex(chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.vertex(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (south)
                    {
                        bufferbuilder.vertex(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.vertex(chunkCoordX + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (east)
                    {
                        bufferbuilder.vertex(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.vertex(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (west)
                    {
                        bufferbuilder.vertex(chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.vertex(chunkCoordX + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }

                    if (levels > lowerYLimitSmaller)
                    {
                        final double addition = upperYLimit/currView.y/(upperYLimit/currView.y - levels/currView.y)*10;
                        levels+= addition > 1 ? addition : 1;
                    }
                    else
                    {
                        levels += 5;
                    }
                }
                if (north)
                {
                    bufferbuilder.vertex(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (south)
                {
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (east)
                {
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (west)
                {
                    bufferbuilder.vertex(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.vertex(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
            }
        }

        tessellator.draw();
        GlStateManager.lineWidth(2.0F);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        tessellator.draw();
        GlStateManager.lineWidth(1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture();
    }
}
