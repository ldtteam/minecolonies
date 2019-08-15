package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Citizen model.
 */
public class CitizenModel extends BipedModel<AbstractEntityCitizen>
{
    public CitizenModel(final float size)
    {
        super(size);
    }

    public CitizenModel() {
        this(0.0F);
    }
}
