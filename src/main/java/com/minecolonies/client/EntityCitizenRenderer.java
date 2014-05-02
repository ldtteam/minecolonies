package com.minecolonies.client;

import com.minecolonies.lib.Constants;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class EntityCitizenRenderer extends Render
{
    private final ResourceLocation texture = new ResourceLocation(Constants.MODID + ":" + "/textures/entity/EntityCitizen.png");

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        GL11.glPushMatrix();
            GL11.glTranslated(var2, var4, var6);
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
            GL11.glScalef(1, 1, 1);
            GL11.glRotatef(0f, 0f, 0f, 0f);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity var1)
    {
        return texture;
    }
}
