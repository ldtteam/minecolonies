package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends RaiderRenderState> extends HumanoidModel<T>
{
    /**
     * Create a model of a specific size.
     *
     */
    public EgyptianModel(final ModelPart part)
    {
        super(part);
    }
}
