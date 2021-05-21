package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen model.
 */
public class CitizenModel extends ModularCitizenModel
{
    public CitizenModel(final float size)
    {
        super(size);
    }

    public CitizenModel()
    {
        this(0.0F);
    }

    @Override
    public void setRotationAngles(@NotNull final AbstractEntityCitizen citizen, float f1, float f2, float f3, float f4, float f5)
    {
        super.setRotationAngles(citizen, f1, f2, f3, f4, f5);
        if (bipedBody.rotateAngleX == 0)
        {
            bipedBody.rotateAngleX = getActualRotation();
        }

        if (bipedHead.rotateAngleX == 0)
        {
            bipedHead.rotateAngleX = getActualRotation();
        }
    }

    /**
     * Override to change body rotation.
     *
     * @return the rotation.
     */
    public float getActualRotation()
    {
        return 0;
    }

    public void setRotation(net.minecraft.client.renderer.model.ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
