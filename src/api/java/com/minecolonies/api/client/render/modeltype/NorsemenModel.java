package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Norsemen model.
 */
public class NorsemenModel<T extends AbstractEntityNorsemen> extends BipedModel<AbstractEntityNorsemen>
{
    /**
     * Create a model of a specific size.
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
