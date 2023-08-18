package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

public class PathfindingDebugRenderer
{
    /**
     * Set of visited nodes.
     */
    public static Set<MNode> lastDebugNodesVisited = new HashSet<>();

    /**
     * Set of not visited nodes.
     */
    public static Set<MNode> lastDebugNodesNotVisited  = new HashSet<>();

    /**
     * Set of nodes that belong to the chosen path.
     */
    public static Set<MNode> lastDebugNodesPath = new HashSet<>();

    /**
     * Render debugging information for the pathfinding system.
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        try
        {
            for (final MNode n : lastDebugNodesVisited)
            {
                debugDrawNode(n, 0xffff0000, ctx);
            }

            for (final MNode n : lastDebugNodesNotVisited)
            {
                debugDrawNode(n, 0xff0000ff, ctx);
            }

            for (final MNode n : lastDebugNodesPath)
            {
                if (n.isReachedByWorker())
                {
                    debugDrawNode(n, 0xffff6600, ctx);
                }
                else
                {
                    debugDrawNode(n, 0xff00ff00, ctx);
                }
            }
        }
        catch (final ConcurrentModificationException exc)
        {
            Log.getLogger().catching(exc);
        }
    }

    private static void debugDrawNode(final MNode n, final int argbColor, final WorldEventContext ctx)
    {
        ctx.getPoseStack().pushPose();
        ctx.getPoseStack().translate(n.pos.getX() + 0.375d, n.pos.getY() + 0.375d, n.pos.getZ() + 0.375d);

        final Entity entity = Minecraft.getInstance().getCameraEntity();
        if (n.pos.closerThan(entity.blockPosition(), 5d))
        {
            renderDebugText(n, ctx);
        }

        ctx.getPoseStack().scale(0.25F, 0.25F, 0.25F);

        WorldRenderMacros.renderBox(ctx.getBufferSource(), ctx.getPoseStack(), BlockPos.ZERO, BlockPos.ZERO, argbColor);

        if (n.parent != null)
        {
            final Matrix4f lineMatrix = ctx.getPoseStack().last().pose();

            final float pdx = n.parent.pos.getX() - n.pos.getX() + 0.125f;
            final float pdy = n.parent.pos.getY() - n.pos.getY() + 0.125f;
            final float pdz = n.parent.pos.getZ() - n.pos.getZ() + 0.125f;

            final VertexConsumer buffer = ctx.getBufferSource().getBuffer(WorldRenderMacros.LINES);

            buffer.vertex(lineMatrix, 0.5f, 0.5f, 0.5f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
            buffer.vertex(lineMatrix, pdx / 0.25f, pdy / 0.25f, pdz / 0.25f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
        }

        ctx.getPoseStack().popPose();
    }

    private static void renderDebugText(@NotNull final MNode n, final WorldEventContext ctx)
    {
        final Font fontrenderer = Minecraft.getInstance().font;

        final String s1 = String.format("F: %.3f [%d]", n.getCost(), n.getCounterAdded());
        final String s2 = String.format("G: %.3f [%d]", n.getScore(), n.getCounterVisited());
        final int i = Math.max(fontrenderer.width(s1), fontrenderer.width(s2)) / 2;

        ctx.getPoseStack().pushPose();
        ctx.getPoseStack().translate(0.0F, 0.75F, 0.0F);

        ctx.getPoseStack().mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        ctx.getPoseStack().scale(-0.014F, -0.014F, 0.014F);
        ctx.getPoseStack().translate(0.0F, 18F, 0.0F);
        final Matrix4f mat = ctx.getPoseStack().last().pose();

        WorldRenderMacros.renderFillRectangle(ctx.getBufferSource(), ctx.getPoseStack(), -i - 1, -5, 0, 2 * i + 2, 17, 0x7f000000);

        ctx.getPoseStack().translate(0.0F, -5F, -0.1F);
        fontrenderer.drawInBatch(s1, -fontrenderer.width(s1) / 2.0f, 1, 0xFFFFFFFF, false, mat, ctx.getBufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        ctx.getPoseStack().translate(0.0F, 8F, -0.1F);
        fontrenderer.drawInBatch(s2, -fontrenderer.width(s2) / 2.0f, 1, 0xFFFFFFFF, false, mat, ctx.getBufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

        ctx.getPoseStack().popPose();
    }
}
