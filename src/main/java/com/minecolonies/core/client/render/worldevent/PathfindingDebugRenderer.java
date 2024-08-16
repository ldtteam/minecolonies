package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

public class PathfindingDebugRenderer
{
    /**
     * Set of visited nodes.
     */
    public static Set<MNode> lastDebugNodesVisited      = new HashSet<>();
    public static Set<MNode> lastDebugNodesVisitedLater = new HashSet<>();
    public static Set<MNode> debugNodesOrgPath          = new HashSet<>();
    public static Set<MNode> debugNodesExtra            = new HashSet<>();

    /**
     * Set of not visited nodes.
     */
    public static Set<MNode> lastDebugNodesNotVisited = new HashSet<>();

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

            for (final MNode n : lastDebugNodesVisitedLater)
            {
                debugDrawNode(n, 0xffff5050, ctx);
            }

            for (final MNode n : debugNodesOrgPath)
            {
                debugDrawNode(n, 0xff808080, ctx);
            }

            for (final MNode n : debugNodesExtra)
            {
                debugDrawNode(n, 0xff9999ff, ctx);
            }

            for (final MNode n : lastDebugNodesNotVisited)
            {
                debugDrawNode(n, 0xff0000ff, ctx);
            }

            for (final MNode n : lastDebugNodesPath)
            {
                if (n.isReachedByWorker())
                {
                    debugDrawNode(n, 0xffffff00, ctx);
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
        ctx.poseStack.pushPose();
        ctx.poseStack.translate(n.x + 0.375d - ctx.cameraPosition.x, n.y + 0.375d - ctx.cameraPosition.y, n.z + 0.375d - ctx.cameraPosition.z);

        final Entity entity = Minecraft.getInstance().getCameraEntity();
        if (BlockPosUtil.distSqr(entity.blockPosition(), n.x, n.y, n.z) < 5d * 5d)
        {
            renderDebugText(n, ctx);
        }

        ctx.poseStack.scale(0.25F, 0.25F, 0.25F);

        ctx.renderBox(WorldEventContext.COLORED_TRIANGLES, BlockPos.ZERO, BlockPos.ZERO, argbColor);

        if (n.parent != null)
        {
            final Matrix4f lineMatrix = ctx.poseStack.last().pose();

            final float pdx = n.parent.x - n.x + 0.125f;
            final float pdy = n.parent.y - n.y + 0.125f;
            final float pdz = n.parent.z - n.z + 0.125f;

            final VertexConsumer buffer = ctx.bufferSource.getBuffer(WorldRenderMacros.LINES);

            buffer.addVertex(lineMatrix, 0.5f, 0.5f, 0.5f).setColor(0.75F, 0.75F, 0.75F, 1.0F);
            buffer.addVertex(lineMatrix, pdx / 0.25f, pdy / 0.25f, pdz / 0.25f).setColor(0.75F, 0.75F, 0.75F, 1.0F);
        }

        ctx.poseStack.popPose();
    }

    private static void renderDebugText(@NotNull final MNode n, final WorldEventContext ctx)
    {
        final Font fontrenderer = ctx.mc.font;

        final String s1 = String.format("C: %.1f", n.getCost());
        final String s2 = String.format("H: %.1f", n.getHeuristic());
        final int i = Math.max(fontrenderer.width(s1), fontrenderer.width(s2)) / 2;

        ctx.poseStack.pushPose();
        ctx.poseStack.translate(0.0F, 0.6F, 0.0F);

        ctx.poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        ctx.poseStack.scale(0.014F, -0.014F, 0.014F);
        final Matrix4f mat = ctx.poseStack.last().pose();

        ctx.renderFillRectangle(-i - 1, -5, 0, 2 * i + 2, 17, 0x7f000000);

        ctx.poseStack.translate(0.0F, -5F, -0.1F);
        fontrenderer.drawInBatch(s1, -fontrenderer.width(s1) / 2.0f, 1, 0xFFFFFFFF, false, mat, ctx.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        ctx.poseStack.translate(0.0F, 8F, -0.1F);
        fontrenderer.drawInBatch(s2, -fontrenderer.width(s2) / 2.0f, 1, 0xFFFFFFFF, false, mat, ctx.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        ctx.poseStack.popPose();
    }
}
