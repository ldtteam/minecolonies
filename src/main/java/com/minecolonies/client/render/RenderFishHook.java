package com.minecolonies.client.render;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.lib.Literals;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Determines how the fish hook is rendered.
 */
public class RenderFishHook extends Render
{
    /**
     * The resource location containing the particle textures (Spawned by the fishHook)
     */
    private static final ResourceLocation texture = new ResourceLocation("textures/particle/particles.png");

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     *
     * @param entity the entity to get the texture from
     * @return a resource location for the texture
     */
    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return getTexture();
    }

    /**
     * Returns the location of an entity's texture.
     * @return the address of the resource
     */
    private static ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFishHook((EntityFishHook) entity, par2, par4, par6, par9);
    }

    /**
     * Render a fishing hook entity in the world
     * This class uses some GL11 stuff
     * and seems hard to document...
     *
     * @param entityFishHook the hook to render
     * @param posX           the x position
     * @param posY           the y position
     * @param posZ           the z position
     * @param angle          the angle thrown
     */
    private void doRenderFishHook(EntityFishHook entityFishHook, double posX, double posY, double posZ, float angle)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) posX, (float) posY, (float) posZ);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(Literals.HALF_MODIFIER, Literals.HALF_MODIFIER, Literals.HALF_MODIFIER);

        this.bindEntityTexture(entityFishHook);

        final Tessellator tessellator = Tessellator.instance;

        double textureSizeU     = (Literals.TEXTURE_ICON_LENGTH) / Literals.TEXTURE_FILE_LENGTH;
        double textureSizeV     = (Literals.TEXTURE_ICON_LENGTH + Literals.TEXTURE_ICON_LENGTH) / Literals.TEXTURE_FILE_LENGTH;
        double textureLocationX = (2 * Literals.TEXTURE_ICON_LENGTH) / Literals.TEXTURE_FILE_LENGTH;
        double textureLocationY = (2 * Literals.TEXTURE_ICON_LENGTH + Literals.TEXTURE_ICON_LENGTH) / Literals.TEXTURE_FILE_LENGTH;

        GL11.glRotatef((float) (Literals.HALF_CIRCKLE - this.renderManager.playerViewY), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(0.0 - Literals.HALF_MODIFIER, 0.0 - Literals.HALF_MODIFIER, 0.0D, textureSizeU, textureLocationY);
        tessellator.addVertexWithUV(1.0 - Literals.HALF_MODIFIER, 0.0 - Literals.HALF_MODIFIER, 0.0D, textureSizeV, textureLocationY);
        tessellator.addVertexWithUV(1.0 - Literals.HALF_MODIFIER, 1.0 - Literals.HALF_MODIFIER, 0.0D, textureSizeV, textureLocationX);
        tessellator.addVertexWithUV(0.0 - Literals.HALF_MODIFIER, 1.0 - Literals.HALF_MODIFIER, 0.0D, textureSizeU, textureLocationX);
        tessellator.draw();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        EntityCitizen citizen = entityFishHook.getCitizen();

        //If the citizen is null (Which he probably is) get the nearest citizen to the fishHook position.
        //Check if he is a fisherman -> Through his texture
        if(citizen==null)
        {
            for (Object citizenX : entityFishHook.worldObj.getEntitiesWithinAABB(EntityCitizen.class, entityFishHook.boundingBox.expand(10,10,10)))
            {
                if(((EntityCitizen) citizenX).getModelID().textureBase.contains("Fisherman"))
                {
                    citizen = (EntityCitizen)citizenX;
                    break;
                }
            }
        }

        if (citizen != null)
        {
            final double orientation      = citizen.getSwingProgress(angle);
            final double finalOrientation = Math.sin(Math.sqrt(orientation) * Math.PI);
            final Vec3   vec3             = Vec3.createVectorHelper(-0.5, 0.03, 0.8);

            vec3.rotateAroundX((float) (-((double)citizen.prevRotationPitch
                                          + ((double)citizen.rotationPitch - (double)citizen.prevRotationPitch) * angle) * Math.PI
                                        / Literals.HALF_CIRCKLE));
            vec3.rotateAroundY((float) (-((double)citizen.prevRotationYaw
                                          + ((double)citizen.rotationYaw - (double)citizen.prevRotationYaw) * angle) * Math.PI
                                        / Literals.HALF_CIRCKLE));
            vec3.rotateAroundY((float) (finalOrientation * 0.5D));
            vec3.rotateAroundX((float) (-finalOrientation * 0.7D));

            double
                    correctedPosX =
                    citizen.prevPosX
                    + (citizen.posX - citizen.prevPosX) * angle
                    + vec3.xCoord;
            double
                    correctedPosY =
                    citizen.prevPosY
                    + (citizen.posY - citizen.prevPosY) * angle
                    + vec3.yCoord;
            double
                    correctedPosZ =
                    citizen.prevPosZ
                    + (citizen.posZ - citizen.prevPosZ) * angle
                    + vec3.zCoord;

            final double distX = entityFishHook.prevPosX + (entityFishHook.posX - entityFishHook.prevPosX) * angle;
            double       distY = entityFishHook.prevPosY + (entityFishHook.posY - entityFishHook.prevPosY) * angle + 0.25;
            double       distZ = entityFishHook.prevPosZ + (entityFishHook.posZ - entityFishHook.prevPosZ) * angle;

            double correctionX = correctedPosX - distX;
            double correctionY = correctedPosY - distY;
            double correctionZ = correctedPosZ - distZ;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);

            for (int i = 0; i <= 16; ++i)
            {
                double crazyVar = (double) i / (double) 16;
                tessellator.addVertex(posX + correctionX * crazyVar, posY + (correctionY+1) * (crazyVar * crazyVar + crazyVar) * 0.5 + 0.25, posZ + correctionZ * crazyVar);
            }

            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }
}
