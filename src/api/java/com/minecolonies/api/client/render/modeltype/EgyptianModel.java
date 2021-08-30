package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends AbstractEntityEgyptian> extends HumanoidModel<AbstractEntityEgyptian>
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
