package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.entity.model.BipedModel;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen model.
 */
public class CitizenModel<T extends AbstractEntityCitizen> extends BipedModel<AbstractEntityCitizen>
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
     * @return the rotation.
     */
    public float getActualRotation()
    {
        return 0;
    }
}
