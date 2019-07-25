package com.minecolonies.coremod.event;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class DebugRendererChunkBorder
{

    private Tuple<Integer, Integer> center = new Tuple<>(0, 0);

    private Map<Tuple<Integer, Integer>, Integer> colonies = new HashMap<>();

    @SubscribeEvent
    public void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final double partialTicks = event.getPartialTicks();
        final PlayerEntity entityplayer = Minecraft.getMinecraft().player;

        if (entityplayer.getHeldItem(EnumHand.MAIN_HAND).getItem() != ModItems.buildTool)
        {
            return;
        }

        final World world = Minecraft.getMinecraft().world;
        final ColonyView view = ColonyManager.getClosestColonyView(world, entityplayer.getPosition());

        if (view == null)
        {
            return;
        }

        if (!center.equals(new Tuple<>(entityplayer.chunkCoordX, entityplayer.chunkCoordZ)))
        {
            center = new Tuple<>(entityplayer.chunkCoordX, entityplayer.chunkCoordZ);
            colonies.clear();
            final int range = Configurations.gameplay.workingRangeTownHallChunks;
            for (int incX = -range; incX <= range; incX += 1)
            {
                for (int incZ = -range; incZ <= range; incZ += 1)
                {
                    final Chunk chunk = world.getChunk(entityplayer.chunkCoordX + incX, entityplayer.chunkCoordZ + incZ);
                    final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);
                    if (cap != null)
                    {
                        colonies.put(new Tuple<>(incX, incZ), cap.getOwningColony());
                    }
                }
            }
        }

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        final double relPlayerX = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * partialTicks;
        final double relPlayerY = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * partialTicks;
        final double relPlayerZ = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * partialTicks;
        final double lowerYLimit = 5 - relPlayerY;
        final double upperYLimit = 255 - relPlayerY;

        final double lowerYLimitSmaller = Math.max(lowerYLimit, entityplayer.posY - 30 - relPlayerY);

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();

        final double chunkCoordX = ((double) (entityplayer.chunkCoordX << 4) - relPlayerX);
        final double chunkCoordZ = ((double) (entityplayer.chunkCoordZ << 4) - relPlayerZ);

        GlStateManager.glLineWidth(1.0F);
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
                        bufferbuilder.pos(chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (south)
                    {
                        bufferbuilder.pos(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(chunkCoordX + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (east)
                    {
                        bufferbuilder.pos(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(chunkCoordX + 16.0D + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (west)
                    {
                        bufferbuilder.pos(chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(chunkCoordX + incX, levels, chunkCoordZ + 16.0D + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }

                    if (levels > lowerYLimitSmaller)
                    {
                        levels+=upperYLimit/relPlayerY/(upperYLimit/relPlayerY - levels/relPlayerY)*10;
                    }
                    else
                    {
                        levels += 5;
                    }
                }
                if (north)
                {
                    bufferbuilder.pos(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (south)
                {
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (east)
                {
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, lowerYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX + 16.0D, upperYLimit, chunkCoordZ + (double) incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (west)
                {
                    bufferbuilder.pos(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, lowerYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(chunkCoordX + (double) incX, upperYLimit, chunkCoordZ + (double) incZ + 16.0D).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
            }
        }

        tessellator.draw();
        GlStateManager.glLineWidth(2.0F);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
    }
}
