package com.minecolonies.coremod.client.render.modularcitizen;

import com.minecolonies.api.client.render.modeltype.modularcitizen.CitizenRenderContainer;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * A simple layer renderer for modular citizen layers that can receive a color modifier.
 */
public class ColorVariedLayer extends AbstractCitizenLayer
{
    private final ColorVariedLayerType layerType;
    public ColorVariedLayer(final IEntityRenderer<AbstractEntityCitizen, ModularCitizenModel> rendererIn, final ColorVariedLayerType layerType)
    {
        super(rendererIn);
        this.layerType = layerType;
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
        final CitizenRenderContainer renderSettings = entityCitizen.getRenderSettings();
        switch(this.layerType)
        {
            case EYES:
                if(renderSettings.modelBase == null || renderSettings.textureEyes == null)
                {
                    return;
                }
                    renderBaseLayer(this.getEntityModel(),
                      renderSettings.textureEyes,
                      matrixStackIn,
                      bufferIn,
                      packedLightIn,
                      entityCitizen,
                      renderSettings.eyesRed,
                      renderSettings.eyesGreen,
                      renderSettings.eyesBlue);
                    break;
            case HAIR:
                if(renderSettings.textureHair == null)
                {
                    return;
                }
                if(renderSettings.modelsCloth != null && renderSettings.modelsCloth.get(CitizenSlots.MODEL_HEAD) != null)
                {
                    renderCopyModel(this.getEntityModel(),
                      renderSettings.modelsCloth.get(CitizenSlots.MODEL_HEAD),
                      renderSettings.textureHair,
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
                      renderSettings.hairRed,
                      renderSettings.hairGreen,
                      renderSettings.hairBlue);
                }
                if(renderSettings.modelsAccessories != null && renderSettings.modelsAccessories.get(CitizenSlots.MODEL_HEAD) != null)
                {
                    renderCopyModel(this.getEntityModel(),
                      renderSettings.modelsAccessories.get(CitizenSlots.MODEL_HEAD),
                      renderSettings.textureHair,
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
                      renderSettings.hairRed,
                      renderSettings.hairGreen,
                      renderSettings.hairBlue);
                }
                break;
            case SUFFIX:
                for (final CitizenSlots slot : CitizenSlots.values())
                {
                    if(renderSettings.texturesSuffix.get(slot) == null)
                    {
                        continue;
                    }
                    renderBaseLayer(this.getEntityModel(),
                      renderSettings.texturesSuffix.get(slot),
                      matrixStackIn,
                      bufferIn,
                      packedLightIn,
                      entityCitizen,
                      renderSettings.suffixRed,
                      renderSettings.suffixGreen,
                      renderSettings.suffixBlue);
                }
                break;
        }
    }

    public enum ColorVariedLayerType
    {
        /**
         * The "eyes" layers contains solely the pupils of the eyes, and is applied first, over the base layer.
         */
        EYES,
        /**
         * The "suffix" layer contains the majority of the body, and applied after the base and eyes layers.
         */
        SUFFIX,
        /**
         * The "hair" layer contains the color-adjustable hair components. It is applied after base, eyes, suffix, and clothing layers.
         */
        HAIR
    }
}