package com.minecolonies.coremod.client.render.modularcitizen;

import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenModel;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A simple layer renderer for modular citizen accessories layers.
 */
public class AccessoryLayer extends AbstractCitizenLayer
{
    public AccessoryLayer(IEntityRenderer<AbstractEntityCitizen, ModularCitizenModel> rendererIn) {
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
        for(final CitizenSlots slot : CitizenSlots.values())
        {
            final ModularCitizenModel model = entityCitizen.getRenderSettings().modelsAccessories.get(slot);
            final ResourceLocation texture = entityCitizen.getRenderSettings().texturesAccessories.get(slot);
            if (model == null || texture == null)
            {
                continue;
            }
            renderCopyModel(this.getEntityModel(),
              model,
              texture,
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
