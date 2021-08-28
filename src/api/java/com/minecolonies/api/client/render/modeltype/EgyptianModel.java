package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.HumanoidModel;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends AbstractEntityEgyptian> extends HumanoidModel<AbstractEntityEgyptian>
{
    /**
     * Create a model of a specific size.
     *
     * @param size the size.
     */
    public EgyptianModel(final float size)
    {
        super(size);
    }

    /**
     * Create a model of the default size.
     */
    public EgyptianModel()
    {
        this(1.0F);
    }
}
