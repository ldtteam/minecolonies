package com.minecolonies.coremod.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Literals;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how the fish hook is rendered.
 */
public class RenderFishHook extends Render<EntityFishHook>
{
    /**
     * The resource location containing the particle textures (Spawned by the fishHook).
     */
    private static final ResourceLocation texture = new ResourceLocation("textures/particle/particles.png");

    /**
     * Required constructor, sets the RenderManager.
     *
     * @param renderManagerIn RenderManager that we use.
     */
    public RenderFishHook(final RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    /**
     * Render a fishing hook entity in the world
     * This class uses some GL11 stuff
     * and seems hard to document...
     *
     * @param entity    the hook to render
     * @param x         the x position
     * @param y         the y position
     * @param z         the z position
     * @param entityYaw the angle thrown
     */
    @Override
    public void doRender(@NotNull final EntityFishHook entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entity);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        GlStateManager.rotate((float) (180.0D - this.renderManager.playerViewY), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        vertexBuffer.pos(-0.5D, -0.5D, 0.0D).tex(0.0625D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexBuffer.pos(0.5D, -0.5D, 0.0D).tex(0.125D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexBuffer.pos(0.5D, 0.5D, 0.0D).tex(0.125D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexBuffer.pos(-0.5D, 0.5D, 0.0D).tex(0.0625D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        AbstractEntityCitizen citizen = entity.getCitizen();

        //If the citizen is null (Which he probably is) get the nearest citizen to the fishHook position.
        //Check if he is a fisherman -> Through his texture
        if (citizen == null)
        {
            for (@NotNull final Object citizenX : CompatibilityUtils.getWorldFromEntity(entity).getEntitiesWithinAABB(EntityCitizen.class, entity.getEntityBoundingBox().expand(10, 10, 10)))
            {
                if (((EntityCitizen) citizenX).getModelID().textureBase.contains("Fisherman"))
                {
                    citizen = (EntityCitizen) citizenX;
                    break;
                }
            }
        }

        if (citizen != null)
        {
            final double orientation = citizen.getSwingProgress(partialTicks);
            final double finalOrientation = Math.sin(Math.sqrt(orientation) * Math.PI);
            @NotNull final Vec3d Vec3d = new Vec3d(-0.36D, 0.03D, 0.35D);

            Vec3d.rotatePitch((float) (-((double) citizen.getPreviousRotationPitch() + ((double) citizen.getRotationPitch() - (double) citizen.getPreviousRotationPitch()) * partialTicks)
                                         * Math.PI / Literals.HALF_CIRCLE));
            Vec3d.rotateYaw((float) (-((double) citizen.getPreviousRotationYaw() + ((double) citizen.getRotationYaw() - (double) citizen.getPreviousRotationYaw())
                                                                            * partialTicks) * Math.PI / Literals.HALF_CIRCLE));
            Vec3d.rotateYaw((float) (finalOrientation * 0.5D));
            Vec3d.rotatePitch((float) (-finalOrientation * 0.7D));

            final double thirdPersonOffset = (citizen.getPreviousRenderYawOffset() + ((double) citizen.getRenderYawOffset() - citizen.getPreviousRenderYawOffset()) * partialTicks)
                                               * Math.PI / Literals.HALF_CIRCLE;
            final double correctedPosX = citizen.getPreviousPosX() + (citizen.getPosX() - citizen.getPreviousPosX()) * (double) partialTicks - MathHelper.cos((float) thirdPersonOffset) * 0.35D
                                           - MathHelper.sin((float) thirdPersonOffset) * 0.8D;
            final double correctedPosY = citizen.getPreviousPosY() + citizen.getEyeHeight() + (citizen.getPosY() - citizen.getPreviousPosY()) * (double) partialTicks - 0.45D;
            final double correctedPosZ = citizen.getPreviousPosZ() + (citizen.getPosZ() - citizen.getPreviousPosZ()) * (double) partialTicks - MathHelper.sin((float) thirdPersonOffset) * 0.35D
                                           + MathHelper.cos((float) thirdPersonOffset) * 0.8D;
            final double eyeHeight = citizen.isSneaking() ? -0.1875D : 0.0D;

            final double distX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            final double distY = entity.posY + 0.25;
            final double distZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

            final double correctionX = correctedPosX - distX;
            final double correctionY = correctedPosY - distY + eyeHeight;
            final double correctionZ = correctedPosZ - distZ;

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            vertexBuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

            for (int l = 0; l <= 16; ++l)
            {
                final double var = (double) l / 16.0;
                vertexBuffer.pos(x + correctionX * var, y + correctionY * (var * var + var) * 0.5D + 0.25D, z + correctionZ * var).color(0, 0, 0, 255).endVertex();
            }

            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     *
     * @param entity the entity to get the texture from
     * @return a resource location for the texture
     */
    @NotNull
    @Override
    protected ResourceLocation getEntityTexture(final EntityFishHook entity)
    {
        return getTexture();
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @return the address of the resource
     */
    @NotNull
    private static ResourceLocation getTexture()
    {
        return texture;
    }
}
