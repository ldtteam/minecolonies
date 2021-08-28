package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen model.
 */
public class CitizenModel<T extends AbstractEntityCitizen> extends HumanoidModel<AbstractEntityCitizen>
{
    public CitizenModel(final float size)
    {
        super(size);
    }

    public CitizenModel()
    {
        this(true);
    }

    public CitizenModel(final boolean legacy)
    {
        super(RenderType::entityCutoutNoCull, 0.0F, 0.0F, 64, legacy ? 32 : 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen citizen, float f1, float f2, float f3, float f4, float f5)
    {
        super.setupAnim(citizen, f1, f2, f3, f4, f5);
        if (body.xRot == 0)
        {
            body.xRot = getActualRotation();
        }

        if (head.xRot == 0)
        {
            head.xRot = getActualRotation();
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
}
