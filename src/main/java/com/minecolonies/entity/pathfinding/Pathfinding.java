package com.minecolonies.entity.pathfinding;

import com.minecolonies.configuration.Configurations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.*;

public class Pathfinding
{
    static private ThreadPoolExecutor executor;
    static private final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<>();

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    static
    {
        executor = new ThreadPoolExecutor(1, Configurations.pathfindingMaxThreadCount, 10, TimeUnit.SECONDS, jobQueue);
    }

    public static Future<PathEntity> enqueue(AbstractPathJob job)
    {
        return executor.submit(job);
    }

    @SideOnly(Side.CLIENT)
    public static void debugDrawNode(Node n, byte r, byte g, byte b)
    {
        GL11.glPushMatrix();
        GL11.glTranslated((double) n.pos.getX() + 0.375, (double) n.pos.getY() + 0.375, (double) n.pos.getZ() + 0.375);

        float f = 1.6F;
        float f1 = 0.016666668F * f / 2;

        //  Nameplate

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        double dx = n.pos.getX() - entity.posX;
        double dy = n.pos.getY() - entity.posY;
        double dz = n.pos.getZ() - entity.posZ;
        if (Math.sqrt(dx*dx + dy*dy + dz*dz) <= 5D)
        {
            String s1 = String.format("F: %.3f [%d]", n.cost, n.counterAdded);
            String s2 = String.format("G: %.3f [%d]", n.score, n.counterVisited);
            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.75F, 0.0F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-f1, -f1, f1);
            GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
            GL11.glDepthMask(false);

            Tessellator   tessellator   = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            // There are several to choose from, look at DefaultVertexFormats for more info
            //todo may need to choose a different Format
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            int i = Math.max(fontrenderer.getStringWidth(s1), fontrenderer.getStringWidth(s2)) / 2;

            //that should set the colors correctly
            worldrenderer.pos((double) (-i - 1), -5.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((double) (-i - 1), 12.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((double) (i + 1), 12.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((double) (i + 1), -5.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
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

        GL11.glScaled(0.25, 0.25, 0.25);

        //Tessellator tessellator = Tessellator.instance;
        //tessellator.startDrawingQuads();

        //tessellator.setColorOpaque(r, g, b);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor3ub(r, g, b);

        //  X+ Facing
        //tessellator.setNormal(1.0F, 0.0F, 0.0F);
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        //  X- Facing
        //tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);

        //  Z-
        //tessellator.setNormal(0.0F, 0.0F, -1.0F);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);

        //  Z+
        //tessellator.setNormal(0.0F, 0.0F, 1.0F);
        GL11.glVertex3d(1.0, 0.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 1.0);

        //  Y+
        //tessellator.setNormal(0.0F, 1.0F, 0.0F);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);

        //  Y-
        //tessellator.setNormal(0.0F, -1.0F, 0.0F);
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        //tessellator.draw();
        GL11.glEnd();

        if (n.parent != null)
        {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor3f(0.75F, 0.75F, 0.75F);

            double pdx = n.parent.pos.getX() - n.pos.getX() + 0.125;
            double pdy = n.parent.pos.getY() - n.pos.getY() + 0.125;
            double pdz = n.parent.pos.getZ() - n.pos.getZ() + 0.125;

            GL11.glVertex3d(0.5, 0.5, 0.5);
            GL11.glVertex3d(pdx / 0.25, pdy / 0.25, pdz / 0.25);

            GL11.glEnd();
        }

        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    public static void debugDraw(double frame)
    {
        if (AbstractPathJob.lastDebugNodesNotVisited == null)
        {
            return;
        }

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glTranslated(-dx, -dy, -dz);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Set<Node> debugNodesNotVisited;
        Set<Node> debugNodesVisited;
        Set<Node> debugNodesPath;

        synchronized (AbstractPathJob.debugNodeMonitor)
        {
            debugNodesNotVisited = AbstractPathJob.lastDebugNodesNotVisited;
            debugNodesVisited = AbstractPathJob.lastDebugNodesVisited;
            debugNodesPath = AbstractPathJob.lastDebugNodesPath;
        }

        try
        {
            for (Node n : debugNodesNotVisited)
            {
                debugDrawNode(n, (byte) 255, (byte) 0, (byte) 0);
            }

            for (Node n : debugNodesVisited)
            {
                debugDrawNode(n, (byte) 0, (byte) 0, (byte) 255);
            }

            if (debugNodesPath != null)
            {
                for (Node n : debugNodesPath)
                {
                    debugDrawNode(n, (byte) 0, (byte) 255, (byte) 0);
                }
            }
        }
        catch (ConcurrentModificationException exc) {}

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
