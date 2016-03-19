package com.minecolonies.client.render;

/**
 * Created by Ray on 19.03.2016.
 */
/*******************************************************************************
 * RenderFishHook.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

        import com.minecolonies.items.MineColoniesEntityFishHook;
        import net.minecraft.client.renderer.Tessellator;
        import net.minecraft.client.renderer.entity.Render;
        import net.minecraft.entity.Entity;
        import net.minecraft.util.MathHelper;
        import net.minecraft.util.ResourceLocation;
        import net.minecraft.util.Vec3;

        import org.lwjgl.opengl.GL11;
        import org.lwjgl.opengl.GL12;

        import cpw.mods.fml.relauncher.Side;
        import cpw.mods.fml.relauncher.SideOnly;

/**
 * Determines how the fish hook is rendered.
 */
@SideOnly(Side.CLIENT)
public class RenderFishHook extends Render
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/particle/particles.png");

    /**
     * Renders the fish hook in the world.
     *
     * @param 	entityFishHook	The fish hook being rendered.
     * @param 	posX			The x position the hook is being rendered at.
     * @param 	posY			The y position the hook is being rendered at.
     * @param 	posZ			The z position the hook is being rendered at.
     * @param 	angle			The angle relative to the angler that the hook is rendered at.
     * @param 	offsetY			The y offset of the hook.
     */
    public void doRenderFishHook(MineColoniesEntityFishHook entityFishHook, double posX, double posY, double posZ, float angle, float offsetY)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        this.bindEntityTexture(entityFishHook);

        final Tessellator tessellator = Tessellator.instance;
        final float textureSizeU = (1 * 8 + 0) / 128.0F;
        final float textureSizeV = (1 * 8 + 8) / 128.0F;
        final float textureLocationX = (2 * 8 + 0) / 128.0F;
        final float textureLocationY = (2 * 8 + 8) / 128.0F;

        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0F - 0.5F, 0.0F - 0.5F, 0.0D, textureSizeU, textureLocationY);
        tessellator.addVertexWithUV(1.0F - 0.5F, 0.0F - 0.5F, 0.0D, textureSizeV, textureLocationY);
        tessellator.addVertexWithUV(1.0F - 0.5F, 1.0F - 0.5F, 0.0D, textureSizeV, textureLocationX);
        tessellator.addVertexWithUV(0.0F - 0.5F, 1.0F - 0.5F, 0.0D, textureSizeU, textureLocationX);
        tessellator.draw();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        /*if (entityFishHook.angler != null)
        {
            final float orientation = MathHelper.sin(MathHelper.sqrt_float(entityFishHook.angler.getSwingProgress(offsetY)) * (float)Math.PI);

            final Vec3 vec3 = Vec3.createVectorHelper(-0.5D, 0.03D, 0.8D);
            vec3.rotateAroundX(-(entityFishHook.angler.prevRotationPitch + (entityFishHook.angler.rotationPitch - entityFishHook.angler.prevRotationPitch) * offsetY) * (float)Math.PI / 180.0F);
            vec3.rotateAroundY(-(entityFishHook.angler.prevRotationYaw + (entityFishHook.angler.rotationYaw - entityFishHook.angler.prevRotationYaw) * offsetY) * (float)Math.PI / 180.0F);
            vec3.rotateAroundY(orientation * 0.5F);
            vec3.rotateAroundX(-orientation * 0.7F);

            double correctedPosX = entityFishHook.angler.prevPosX + (entityFishHook.angler.posX - entityFishHook.angler.prevPosX) * (double)offsetY + vec3.xCoord;
            double correctedPosY = entityFishHook.angler.prevPosY + (entityFishHook.angler.posY - entityFishHook.angler.prevPosY) * (double)offsetY + vec3.yCoord;
            double correctedPosZ = entityFishHook.angler.prevPosZ + (entityFishHook.angler.posZ - entityFishHook.angler.prevPosZ) * (double)offsetY + vec3.zCoord;

            float scale = 0.7F;

            if (entityFishHook.angler instanceof EntityPlayerChild)
            {
                final int age = ((AbstractChild)entityFishHook.angler).age;
                scale = 0.55F + 0.39F / MCA.getInstance().getModProperties().kidGrowUpTimeMinutes * age;
            }

            final float offsetYaw = (entityFishHook.angler.prevRenderYawOffset + (entityFishHook.angler.renderYawOffset - entityFishHook.angler.prevRenderYawOffset) * offsetY) * (float)Math.PI / 180.0F;
            final double sinOffsetYaw = MathHelper.sin(offsetYaw);
            final double cosOffsetYaw = MathHelper.cos(offsetYaw);
            correctedPosX = entityFishHook.angler.prevPosX + (entityFishHook.angler.posX - entityFishHook.angler.prevPosX) * offsetY - cosOffsetYaw * 0.35D - sinOffsetYaw * 0.85D;
            correctedPosY = entityFishHook.angler.prevPosY + scale * 1.6 + (entityFishHook.angler.posY - entityFishHook.angler.prevPosY) * offsetY - 0.45D;
            correctedPosZ = entityFishHook.angler.prevPosZ + (entityFishHook.angler.posZ - entityFishHook.angler.prevPosZ) * offsetY - sinOffsetYaw * 0.35D + cosOffsetYaw * 0.85D;

            final double distX = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * (double)offsetY;
            final double distY = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * (double)offsetY + 0.25D;
            final double distZ = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * (double)offsetY;
            final double correctionX = (double)((float)(correctedPosX - distX));
            final double correctionY = (double)((float)(correctedPosY - distY));
            final double correctionZ = (double)((float)(correctedPosZ - distZ));

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);

            for (int loop = 0; loop <= 16; ++loop)
            {
                final float unknown = (float)loop / (float)16;
                tessellator.addVertex(posX + correctionX * unknown, posY + correctionY * (unknown * unknown + unknown) * 0.5D + 0.25D, posZ + correctionZ * unknown);
            }

            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }*/
    }

    @Override
    public void doRender(Entity entity, double posX, double posY, double posZ, float angle, float offsetY)
    {
        this.doRenderFishHook((MineColoniesEntityFishHook)entity, posX, posY, posZ, angle, offsetY);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return TEXTURE;
    }
}
