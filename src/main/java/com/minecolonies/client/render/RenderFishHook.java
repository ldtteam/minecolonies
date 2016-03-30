package com.minecolonies.client.render;

/*******************************************************************************
 * RenderFishHook.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

        import com.minecolonies.entity.EntityFishHook;
        import net.minecraft.client.renderer.Tessellator;
        import net.minecraft.client.renderer.entity.Render;
        import net.minecraft.entity.Entity;
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
    private static final ResourceLocation texture = new ResourceLocation("textures/particle/particles.png");

    private void doRenderFishHook(EntityFishHook entityFishHook, double posX, double posY, double posZ, float angle)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX,(float)posY,(float)posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        this.bindEntityTexture(entityFishHook);

        final Tessellator tessellator = Tessellator.instance;

        double textureSizeU = (8) / 128.0;
        double textureSizeV = (8 + 8) / 128.0;
        double textureLocationX = (2 * 8) / 128.0;
        double textureLocationY = (2 * 8 + 8) / 128.0;

        GL11.glRotatef((float) (180.0D - this.renderManager.playerViewY), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0 - 0.5, 0.0 - 0.5, 0.0D, textureSizeU, textureLocationY);
        tessellator.addVertexWithUV(1.0 - 0.5, 0.0 - 0.5, 0.0D, textureSizeV, textureLocationY);
        tessellator.addVertexWithUV(1.0 - 0.5, 1.0 - 0.5, 0.0D, textureSizeV, textureLocationX);
        tessellator.addVertexWithUV(0.0 - 0.5, 1.0 - 0.5, 0.0D, textureSizeU, textureLocationX);
        tessellator.draw();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        if(entityFishHook.citizen != null)
        {
            final double orientation = entityFishHook.citizen.getSwingProgress(angle);
            final double finalOrientation = Math.sin(Math.sqrt(orientation) * Math.PI);
            final Vec3 vec3 = Vec3.createVectorHelper(-0.5, 0.03, 0.8);

            vec3.rotateAroundX((float) (-(entityFishHook.citizen.prevRotationPitch + (entityFishHook.citizen.rotationPitch - entityFishHook.citizen.prevRotationPitch) * angle) * Math.PI / 180.0D));
            vec3.rotateAroundY((float) (-(entityFishHook.citizen.prevRotationYaw + (entityFishHook.citizen.rotationYaw - entityFishHook.citizen.prevRotationYaw) * angle) * Math.PI / 180.0D));
            vec3.rotateAroundY((float) (finalOrientation * 0.5D));
            vec3.rotateAroundX((float) (-finalOrientation * 0.7D));

            double correctedPosX = entityFishHook.citizen.prevPosX + (entityFishHook.citizen.posX - entityFishHook.citizen.prevPosX) * angle + vec3.xCoord;
            double correctedPosY = entityFishHook.citizen.prevPosY + (entityFishHook.citizen.posY - entityFishHook.citizen.prevPosY) * angle + vec3.yCoord;
            double correctedPosZ = entityFishHook.citizen.prevPosZ + (entityFishHook.citizen.posZ - entityFishHook.citizen.prevPosZ) * angle + vec3.zCoord;

            double scale = (double)entityFishHook.citizen.getEyeHeight();

            if(this.renderManager.options.thirdPersonView > 0)
            {
                double f11 = ((double)entityFishHook.citizen.prevRenderYawOffset + ((double)entityFishHook.citizen.renderYawOffset - (double)entityFishHook.citizen.prevRenderYawOffset) * (double)angle) * Math.PI / 180.0D;
                double d7 = Math.sin(f11);
                double d8 = Math.cos(f11);

                correctedPosX = entityFishHook.citizen.prevPosX + (entityFishHook.citizen.posX - entityFishHook.citizen.prevPosX) * angle - d8 * 0.35 - d7 * 0.85;
                correctedPosY = entityFishHook.citizen.prevPosY + scale + (entityFishHook.citizen.posY - entityFishHook.citizen.prevPosY) * angle - 0.45;
                correctedPosZ = entityFishHook.citizen.prevPosZ + (entityFishHook.citizen.posZ - entityFishHook.citizen.prevPosZ) * angle - d7 * 0.35 + d8 * 0.85;
            }

            final double distX = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * angle;
            double distY = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * angle + 0.25;
            double distZ = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * angle;

            double correctionX = correctedPosX - distX;
            double correctionY = correctedPosY - distY;
            double correctionZ = correctedPosZ - distZ;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);

            for(int i = 0; i <= 16; ++i)
            {
                double crazyVar = (double)i / (double)16;
                tessellator.addVertex(posX + correctionX * crazyVar, posY + correctionY * (crazyVar * crazyVar + crazyVar) * 0.5 + 0.25, posZ + correctionZ * crazyVar);
            }

            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    private ResourceLocation getTexture()
    {
        return texture;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return this.getTexture();
    }

    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFishHook((EntityFishHook)entity, par2, par4, par6, par9);
    }
}