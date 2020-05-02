package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends AbstractEntityEgyptian> extends BipedModel<AbstractEntityEgyptian>
{
    public EgyptianModel(final float size)
    {
        super(size);
    }

    public EgyptianModel()
    {
        this(1.0F);
    }
}
