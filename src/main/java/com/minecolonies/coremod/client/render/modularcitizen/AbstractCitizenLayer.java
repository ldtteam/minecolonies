package com.minecolonies.coremod.client.render.modularcitizen;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Simple class to contain shared layer methods specific to MineColonies Citizens, such as halloween mode.
 */
public abstract class AbstractCitizenLayer extends LayerRenderer<AbstractEntityCitizen, ModularCitizenModel>
{
    public AbstractCitizenLayer(final IEntityRenderer<AbstractEntityCitizen, ModularCitizenModel> entityRendererIn)
    {
        super(entityRendererIn);
    }

    /**
     * A special variant of the renderCopyCutoutModel function, with an alpha channel exposed.
     * @param model                The model to draw.
     * @param texture              The texture to draw.
     * @param matrixStackIn        The matrix stack.
     * @param bufferIn             The buffer.
     * @param packedLightIn        Packed Lighting information.
     * @param entityCitizen        The citizen data.
     * @param red                  Red modifier
     * @param green                Green modifier.
     * @param blue                 Blue modifier.
     */
    protected void renderBaseLayer(
      @NotNull ModularCitizenModel model,
      ResourceLocation texture,
      MatrixStack matrixStackIn,
      IRenderTypeBuffer bufferIn,
      int packedLightIn,
      AbstractEntityCitizen entityCitizen,
      float red,
      float green,
      float blue)
    {
        if (!entityCitizen.isInvisible())
        {
            final IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(texture));
            model.render(matrixStackIn, ivertexbuilder, packedLightIn, LivingRenderer.getPackedOverlay(entityCitizen, 0.0F), red, green, blue, entityCitizen.getRenderSettings().alpha);
        }
    }

    /**
     * A special variant of renderCopyCutout handling alpha channels.
     * @param modelParent          The parent to clone data from.
     * @param modelChild           The child to clone data to, and to draw.
     * @param texture              The texture to draw.
     * @param matrixStackIn        The matrix stack.
     * @param bufferIn             The buffer.
     * @param packedLightIn        Packed Lighting information.
     * @param entityCitizen        The citizen data.
     * @param limbSwing            If the limb is swinging
     * @param limbSwingAmount      Amount the limb is swinging
     * @param ageInTicks           Lifespan
     * @param partialTicks         Duration
     * @param netHeadYaw           Head yaw
     * @param headPitch            Head pitch.
     * @param red                  Red modifier
     * @param green                Green modifier.
     * @param blue                 Blue modifier.
     */
    protected void renderCopyModel(
      @NotNull ModularCitizenModel modelParent,
      @NotNull ModularCitizenModel modelChild,
      ResourceLocation texture,
      MatrixStack matrixStackIn,
      IRenderTypeBuffer bufferIn,
      int packedLightIn,
      AbstractEntityCitizen entityCitizen,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float partialTicks,
      float netHeadYaw,
      float headPitch,
      float red,
      float green,
      float blue)
    {
        if (!entityCitizen.isInvisible())
        {
            modelParent.copyModelAttributesTo(modelChild);
            modelChild.setLivingAnimations(entityCitizen, limbSwing, limbSwingAmount, partialTicks);
            modelChild.setRotationAngles(entityCitizen, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            final IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(texture, false));
            modelChild.render(matrixStackIn, ivertexbuilder, packedLightIn, LivingRenderer.getPackedOverlay(entityCitizen, 0.0F), red, green, blue, entityCitizen.getRenderSettings().alpha);
        }
    }
}
