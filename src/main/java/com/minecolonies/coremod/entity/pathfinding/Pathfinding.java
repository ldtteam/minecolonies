package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Static class the handles all the Pathfinding.
 */
public final class Pathfinding
{
    private static final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<>();
    private static final ResourceLocation        TEXTURE  = new ResourceLocation("textures/gui/widgets.png");
    private static final ThreadPoolExecutor executor;
    static
    {
        executor = new ThreadPoolExecutor(1, Configurations.pathfindingMaxThreadCount, 10, TimeUnit.SECONDS, jobQueue);
    }
    private Pathfinding()
    {
        //Hides default constructor.
    }

    /**
     * Add a job to the queue for processing.
     *
     * @param job PathJob
     * @return a Future containing the Path
     */
    public static Future<Path> enqueue(@NotNull final AbstractPathJob job)
    {
        return executor.submit(job);
    }

    /**
     * Render debugging information for the pathfinding system.
     *
     * @param frame entity movement weight.
     */
    @SideOnly(Side.CLIENT)
    public static void debugDraw(final double frame)
    {
        if (AbstractPathJob.lastDebugNodesNotVisited == null)
        {
            return;
        }

        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        final double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
        final double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
        final double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glTranslated(-dx, -dy, -dz);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        final Set<Node> debugNodesNotVisited;
        final Set<Node> debugNodesVisited;
        final Set<Node> debugNodesPath;

        synchronized (AbstractPathJob.debugNodeMonitor)
        {
            debugNodesNotVisited = AbstractPathJob.lastDebugNodesNotVisited;
            debugNodesVisited = AbstractPathJob.lastDebugNodesVisited;
            debugNodesPath = AbstractPathJob.lastDebugNodesPath;
        }

        try
        {
            for (@NotNull final Node n : debugNodesNotVisited)
            {
                debugDrawNode(n, (byte) 255, (byte) 0, (byte) 0);
            }

            for (@NotNull final Node n : debugNodesVisited)
            {
                debugDrawNode(n, (byte) 0, (byte) 0, (byte) 255);
            }

            if (debugNodesPath != null)
            {
                for (@NotNull final Node n : debugNodesPath)
                {
                    debugDrawNode(n, (byte) 0, (byte) 255, (byte) 0);
                }
            }
        }
        catch (final ConcurrentModificationException exc)
        {
            Log.getLogger().catching(exc);
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    private static void debugDrawNode(@NotNull final Node n, final byte r, final byte g, final byte b)
    {
        GL11.glPushMatrix();
        GL11.glTranslated((double) n.pos.getX() + 0.375, (double) n.pos.getY() + 0.375, (double) n.pos.getZ() + 0.375);

        final float f = 1.6F;
        final float f1 = (float) (0.016666668D * f / 2);

        //  Nameplate

        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        final double dx = n.pos.getX() - entity.posX;
        final double dy = n.pos.getY() - entity.posY;
        final double dz = n.pos.getZ() - entity.posZ;
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 5D)
        {
            renderDebugText(n, f1);
        }

        GL11.glScaled(0.25, 0.25, 0.25);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor3ub(r, g, b);

        //  X+ Facing
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        //  X- Facing
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);

        //  Z-
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);

        //  Z+
        GL11.glVertex3d(1.0, 0.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 1.0);

        //  Y+
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);

        //  Y-
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        GL11.glEnd();

        if (n.parent != null)
        {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor3f(0.75F, 0.75F, 0.75F);

            final double pdx = n.parent.pos.getX() - n.pos.getX() + 0.125;
            final double pdy = n.parent.pos.getY() - n.pos.getY() + 0.125;
            final double pdz = n.parent.pos.getZ() - n.pos.getZ() + 0.125;

            GL11.glVertex3d(0.5, 0.5, 0.5);
            GL11.glVertex3d(pdx / 0.25, pdy / 0.25, pdz / 0.25);

            GL11.glEnd();
        }

        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    private static void renderDebugText(@NotNull final Node n, final float f1)
    {
        final String s1 = String.format("F: %.3f [%d]", n.getCost(), n.getCounterAdded());
        final String s2 = String.format("G: %.3f [%d]", n.getScore(), n.getCounterVisited());
        final FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.75F, 0.0F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-f1, -f1, f1);
        GL11.glTranslatef(0.0F, (float) (0.25D / f1), 0.0F);
        GL11.glDepthMask(false);

        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer VertexBuffer = tessellator.getBuffer();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        VertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        final int i = Math.max(fontrenderer.getStringWidth(s1), fontrenderer.getStringWidth(s2)) / 2;

        //that should set the colors correctly
        VertexBuffer.pos((double) (-i - 1), -5.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        VertexBuffer.pos((double) (-i - 1), 12.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        VertexBuffer.pos((double) (i + 1), 12.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        VertexBuffer.pos((double) (i + 1), -5.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        GL11.glDepthMask(true);
        GL11.glTranslatef(0.0F, -5F, 0.0F);
        fontrenderer.drawString(s1, -fontrenderer.getStringWidth(s1) / 2, 0, 553648127);
        GL11.glTranslatef(0.0F, 8F, 0.0F);
        fontrenderer.drawString(s2, -fontrenderer.getStringWidth(s2) / 2, 0, 553648127);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
