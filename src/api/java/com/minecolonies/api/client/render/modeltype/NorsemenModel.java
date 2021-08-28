package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import net.minecraft.client.model.HumanoidModel;

/**
 * Norsemen model.
 */
public class NorsemenModel extends HumanoidModel<AbstractEntityNorsemen>
{
    /**
     * Create a model of a specific size.
     *
     * @param size the size.
     */
    public NorsemenModel(final float size)
    {
        super(size);
    }

    /**
     * Create a model of the default size.
     */
    public NorsemenModel()
    {
        this(1.0F);
    }
}
