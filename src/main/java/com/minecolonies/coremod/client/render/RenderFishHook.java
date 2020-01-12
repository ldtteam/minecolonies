package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how the fish hook is rendered.
 */
@OnlyIn(Dist.CLIENT)
public class RenderFishHook extends EntityRenderer<NewBobberEntity>
{
    /**
     * The resource location containing the particle textures (Spawned by the fishHook).
     */
    private static final ResourceLocation texture = new ResourceLocation("textures/entity/fishing_hook.png");

    /**
     * Required constructor, sets the RenderManager.
     *
     * @param renderManagerIn RenderManager that we use.
     */
    public RenderFishHook(final EntityRendererManager renderManagerIn)
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
    public void doRender(@NotNull final NewBobberEntity entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks)
    {
        EntityCitizen citizen = entity.getAngler();
        if (citizen != null && !this.renderOutlines) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)x, (float)y, (float)z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            this.bindEntityTexture(entity);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            float f = 1.0F;
            float f1 = 0.5F;
            float f2 = 0.5F;
            GlStateManager.rotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            if (this.renderOutlines) {
                GlStateManager.enableColorMaterial();
                GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
            bufferbuilder.pos(-0.5D, -0.5D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(0.5D, -0.5D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(0.5D, 0.5D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(-0.5D, 0.5D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
            tessellator.draw();
            if (this.renderOutlines) {
                GlStateManager.tearDownSolidRenderingTextureCombine();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            int i = citizen.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
            ItemStack itemstack = citizen.getHeldItemMainhand();
            if (!(itemstack.getItem() instanceof net.minecraft.item.FishingRodItem)) {
                i = -i;
            }

            float f3 = citizen.getSwingProgress(partialTicks);
            float f4 = MathHelper.sin(MathHelper.sqrt(f3) * (float)Math.PI);
            float f5 = MathHelper.lerp(partialTicks, citizen.prevRenderYawOffset, citizen.renderYawOffset) * ((float)Math.PI / 180F);
            double d0 = (double)MathHelper.sin(f5);
            double d1 = (double)MathHelper.cos(f5);
            double d2 = (double)i * 0.35D;
            double d3 = 0.8D;
            double d4 = MathHelper.lerp((double)partialTicks, citizen.prevPosX, citizen.posX) - d1 * d2 - d0 * 0.8D;
            double d5 = citizen.prevPosY + (double)citizen.getEyeHeight() + (citizen.posY - citizen.prevPosY) * (double)partialTicks - 0.45D;
            double d6 = MathHelper.lerp((double)partialTicks, citizen.prevPosZ, citizen.posZ) - d0 * d2 + d1 * 0.8D;
            double d7 = citizen.shouldRenderSneaking() ? -0.1875D : 0.0D;

            double d13 = MathHelper.lerp((double)partialTicks, entity.prevPosX, entity.posX);
            double d14 = MathHelper.lerp((double)partialTicks, entity.prevPosY, entity.posY) + 0.25D;
            double d9 = MathHelper.lerp((double)partialTicks, entity.prevPosZ, entity.posZ);
            double d10 = (double)((float)(d4 - d13));
            double d11 = (double)((float)(d5 - d14)) + d7;
            double d12 = (double)((float)(d6 - d9));
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            int j = 16;

            for(int k = 0; k <= 16; ++k) {
                float f6 = (float)k / 16.0F;
                bufferbuilder.vertex(x + d10 * (double)f6, y + d11 * (double)(f6 * f6 + f6) * 0.5D + 0.25D, z + d12 * (double)f6).color(0, 0, 0, 255).endVertex();
            }

            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
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
    public ResourceLocation getEntityTexture(@NotNull final NewBobberEntity entity)
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
