package com.minecolonies.coremod.client.render.modularcitizen;

import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A simple layer renderer for the modular citizen illness layer.
 */
public class IllnessLayer extends AbstractCitizenLayer
{
    public IllnessLayer(IEntityRenderer<AbstractEntityCitizen, ModularCitizenModel> rendererIn) {
        super(rendererIn);
    }

    @Override
    public void render(
      @NotNull final MatrixStack matrixStackIn,
      @NotNull final IRenderTypeBuffer bufferIn,
      final int packedLightIn,
      @NotNull final AbstractEntityCitizen entityCitizen,
      final float limbSwing,
      final float limbSwingAmount,
      final float partialTicks,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch)
    {
        if(entityCitizen.getCitizenDiseaseHandler().isSick())
        {
            renderCopyModel(this.getEntityModel(),
              entityCitizen.getRenderSettings().modelBase,
              entityCitizen.getRenderSettings().textureIllness,
              matrixStackIn,
              bufferIn,
              packedLightIn,
              entityCitizen,
              limbSwing,
              limbSwingAmount,
              ageInTicks,
              partialTicks,
              netHeadYaw,
              headPitch,
              1.0F,
              1.0F,
              1.0F);
        }
    }
}