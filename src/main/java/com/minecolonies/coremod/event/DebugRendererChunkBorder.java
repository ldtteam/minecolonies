package com.minecolonies.coremod.event;

import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.render.MRenderTypes;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
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
            final int range = MineColonies.getConfig().getCommon().maxColonySize.get();
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

        final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        final IVertexBuilder bufferbuilder = buffer.getBuffer(MRenderTypes.customLineRenderer());

        final Vec3d currView = Minecraft.getInstance().getRenderManager().info.getProjectedView();
        final float lowerYLimit = (float) (5 - currView.y);
        final float upperYLimit = (float) (255 - currView.y);

        final double lowerYLimitSmaller = Math.max(lowerYLimit, currView.y - 30 - currView.y);

        final float chunkCoordX = (float) ((float) (player.chunkCoordX << 4) - currView.x);
        final float chunkCoordZ = (float) ((float) (player.chunkCoordZ << 4) - currView.z);

        final MatrixStack stack = event.getMatrixStack();
        stack.push();
        final Matrix4f matrix = stack.getLast().getMatrix();

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

                float levels = lowerYLimit;
                while (levels <= upperYLimit)
                {
                    if (north)
                    {
                        bufferbuilder.pos(matrix, chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(matrix,chunkCoordX + 16.0f + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (south)
                    {
                        bufferbuilder.pos(matrix,chunkCoordX + 16.0f + incX, levels, chunkCoordZ + 16.0f + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(matrix,chunkCoordX + incX, levels, chunkCoordZ + 16.0f + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (east)
                    {
                        bufferbuilder.pos(matrix,chunkCoordX + 16.0f + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(matrix,chunkCoordX + 16.0f + incX, levels, chunkCoordZ + 16.0f + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                    }
                    if (west)
                    {
                        bufferbuilder.pos(matrix,chunkCoordX + incX, levels, chunkCoordZ + incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                        bufferbuilder.pos(matrix,chunkCoordX + incX, levels, chunkCoordZ + 16.0f + incZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
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
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, lowerYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, upperYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, lowerYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, upperYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (south)
                {
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, lowerYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, upperYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, lowerYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, upperYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (east)
                {
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, lowerYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, upperYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, lowerYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX + 16.0f, upperYLimit, chunkCoordZ +  incZ).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
                if (west)
                {
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, lowerYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, upperYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, lowerYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.5F).endVertex();
                    bufferbuilder.pos(matrix,chunkCoordX +  incX, upperYLimit, chunkCoordZ +  incZ + 16.0f).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
                }
            }
        }
        stack.pop();
        buffer.finish();
    }
}
