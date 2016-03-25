package com.minecolonies.client.render;

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

    private void doRenderFishHook(MineColoniesEntityFishHook entityFishHook, double par2, double par4, double par6, float par9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2,(float)par4,(float)par6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entityFishHook);
        Tessellator tessellator = Tessellator.instance;
        byte b0 = 1;
        byte b1 = 2;
        double f2 = (b0 * 8 + 0) / 128.0F;
        double f3 = (b0 * 8 + 8) / 128.0F;
        double f4 = (b1 * 8 + 0) / 128.0F;
        double f5 = (b1 * 8 + 8) / 128.0F;
        double f6 = 1.0F;
        double f7 = 0.5F;
        double f8 = 0.5F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0F - f7, 0.0F - f8, 0.0D, f2, f5);
        tessellator.addVertexWithUV(f6 - f7, 0.0F - f8, 0.0D, f3, f5);
        tessellator.addVertexWithUV(f6 - f7, 1.0F - f8, 0.0D, f3, f4);
        tessellator.addVertexWithUV(0.0F - f7, 1.0F - f8, 0.0D, f2, f4);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        if(entityFishHook.citizen != null)
        {
            double f9 = entityFishHook.citizen.getSwingProgress(par9);
            double f10 = Math.sin(Math.sqrt(f9) * Math.PI);
            Vec3 vec3 = Vec3.createVectorHelper(-0.5D, 0.03D, 0.8D);
            vec3.rotateAroundX(-(entityFishHook.citizen.prevRotationPitch + (entityFishHook.citizen.rotationPitch - entityFishHook.citizen.prevRotationPitch) * par9) * (float)Math.PI / 180.0F);
            vec3.rotateAroundY(-(entityFishHook.citizen.prevRotationYaw + (entityFishHook.citizen.rotationYaw - entityFishHook.citizen.prevRotationYaw) * par9) * (float)Math.PI / 180.0F);
            vec3.rotateAroundY((float)f10 * 0.5F);
            vec3.rotateAroundX((float)-f10 * 0.7F);
            double d3 = entityFishHook.citizen.prevPosX + (entityFishHook.citizen.posX - entityFishHook.citizen.prevPosX) * par9 + vec3.xCoord;
            double d4 = entityFishHook.citizen.prevPosY + (entityFishHook.citizen.posY - entityFishHook.citizen.prevPosY) * par9 + vec3.yCoord;
            double d5 = entityFishHook.citizen.prevPosZ + (entityFishHook.citizen.posZ - entityFishHook.citizen.prevPosZ) * par9 + vec3.zCoord;
            double d6 = (double)entityFishHook.citizen.getEyeHeight();

            if(this.renderManager.options.thirdPersonView > 0)
            {
                double f11 = (entityFishHook.citizen.prevRenderYawOffset + (entityFishHook.citizen.renderYawOffset - entityFishHook.citizen.prevRenderYawOffset) * par9) * Math.PI / 180.0F;
                double d7 = Math.sin(f11);
                double d8 = Math.cos(f11);

                d3 = entityFishHook.citizen.prevPosX + (entityFishHook.citizen.posX - entityFishHook.citizen.prevPosX) * par9 - d8 * 0.35D - d7 * 0.85D;
                d4 = entityFishHook.citizen.prevPosY + d6 + (entityFishHook.citizen.posY - entityFishHook.citizen.prevPosY) * par9 - 0.45D;
                d5 = entityFishHook.citizen.prevPosZ + (entityFishHook.citizen.posZ - entityFishHook.citizen.prevPosZ) * par9 - d7 * 0.35D + d8 * 0.85D;
            }

            double d9 = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * par9;
            double d10 = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * par9 + 0.25D;
            double d11 = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * par9;
            double d12 = ((d3 - d9));
            double d13 = ((d4 - d10));
            double d14 = ((d5 - d11));
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);
            byte b2 = 16;

            for(int i = 0; i <= b2; ++i)
            {
                double f12 = (double)i / (double)b2;
                tessellator.addVertex(par2 + d12 * f12, par4 + d13 * (f12 * f12 + f12) * 0.5D + 0.25D, par6 + d14 * f12);
            }

            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    private ResourceLocation getTexture(MineColoniesEntityFishHook entityFishHook)
    {
        return texture;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return this.getTexture((MineColoniesEntityFishHook)entity);
    }

    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFishHook((MineColoniesEntityFishHook)entity, par2, par4, par6, par9);
    }
}