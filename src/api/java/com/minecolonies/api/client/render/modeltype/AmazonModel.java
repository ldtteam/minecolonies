package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Amazon model.
 */
public class AmazonModel<T extends AbstractEntityAmazon> extends BipedModel<AbstractEntityAmazon>
{
    /**
     * Create a model of a specific size.
     * @param size the size.
     */
    public AmazonModel(final float size)
    {
        super(size);
    }

    /**
     * Create a model of the default size.
     */
    public AmazonModel()
    {
        this(1.0F);
    }
}
