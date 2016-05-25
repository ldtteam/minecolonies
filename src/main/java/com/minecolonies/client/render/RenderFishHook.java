package com.minecolonies.client.render;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.lib.Literals;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

/**
 * Determines how the fish hook is rendered.
 */
public class RenderFishHook extends Render<EntityFishHook>
{
	
    public RenderFishHook(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

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
    protected ResourceLocation getEntityTexture(EntityFishHook entity)
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
     * Render a fishing hook entity in the world
     * This class uses some GL11 stuff
     * and seems hard to document...
     *
     * @param entity the hook to render
     * @param x           the x position
     * @param y           the y position
     * @param z           the z position
     * @param entityYaw   the angle thrown
     */
    @Override
    public void doRender(EntityFishHook entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entity);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        worldrenderer.pos(-0.5D, -0.5D, 0.0D).tex(0.0625D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, -0.5D, 0.0D).tex(0.125D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, 0.5D, 0.0D).tex(0.125D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(-0.5D, 0.5D, 0.0D).tex(0.0625D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        EntityCitizen citizen = entity.getCitizen();

        //If the citizen is null (Which he probably is) get the nearest citizen to the fishHook position.
        //Check if he is a fisherman -> Through his texture
        if(citizen==null)
        {
            for (Object citizenX : entity.worldObj.getEntitiesWithinAABB(EntityCitizen.class, entity.getEntityBoundingBox().expand(10,10,10)))
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
            final double orientation      = citizen.getSwingProgress(partialTicks);
            final double finalOrientation = Math.sin(Math.sqrt(orientation) * Math.PI);
            final Vec3   vec3             = new Vec3(-0.36D, 0.03D, 0.35D);

            vec3.rotatePitch((float) (-((double)citizen.prevRotationPitch + ((double)citizen.rotationPitch - (double)citizen.prevRotationPitch) * partialTicks) * Math.PI / Literals.HALF_CIRCKLE));
            vec3.rotateYaw((float) (-((double)citizen.prevRotationYaw + ((double)citizen.rotationYaw - (double)citizen.prevRotationYaw) * partialTicks) * Math.PI / Literals.HALF_CIRCKLE));
            vec3.rotateYaw((float) (finalOrientation * 0.5D));
            vec3.rotatePitch((float) (-finalOrientation * 0.7D));

            double thirdPersonOffset = (citizen.prevRenderYawOffset + (citizen.renderYawOffset - citizen.prevRenderYawOffset) * partialTicks) * 3.1415927F / Literals.HALF_CIRCKLE;
            double correctedPosX = citizen.prevPosX + (citizen.posX - citizen.prevPosX) * (double)partialTicks - MathHelper.cos((float)thirdPersonOffset) * 0.35D - MathHelper.sin((float)thirdPersonOffset) * 0.8D;
            double correctedPosY = citizen.prevPosY + citizen.getEyeHeight() + (citizen.posY - citizen.prevPosY) * (double)partialTicks - 0.45D;
            double correctedPosZ = citizen.prevPosZ + (citizen.posZ - citizen.prevPosZ) * (double)partialTicks - MathHelper.sin((float)thirdPersonOffset) * 0.35D + MathHelper.cos((float)thirdPersonOffset) * 0.8D;
            double eyeHeight = citizen.isSneaking()?-0.1875D:0.0D;

            final double distX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double       distY = entity.posY  + 0.25;
            double       distZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

            double correctionX = correctedPosX - distX;
            double correctionY = correctedPosY - distY + eyeHeight;
            double correctionZ = correctedPosZ - distZ;

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);

            for(int l = 0; l <= 16; ++l)
            {
                double var = (double)l / 16.0;
                worldrenderer.pos(x + correctionX * var, y + correctionY * (var * var + var) * 0.5D + 0.25D, z + correctionZ * var).color(0, 0, 0, 255).endVertex();
            }

            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
