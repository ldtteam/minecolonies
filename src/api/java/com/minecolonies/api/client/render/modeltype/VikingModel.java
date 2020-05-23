package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityViking;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Viking model.
 */
public class VikingModel<T extends AbstractEntityViking> extends BipedModel<AbstractEntityViking>
{
    /**
     * Create a model of a specific size.
     * @param size the size.
     */
    public VikingModel(final float size)
    {
        super(size);
    }

    /**
     * Create a model of the default size.
     */
    public VikingModel()
    {
        this(1.0F);
    }
}
