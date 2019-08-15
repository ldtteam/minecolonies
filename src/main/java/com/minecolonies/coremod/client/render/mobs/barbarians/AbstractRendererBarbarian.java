package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;

/**
 * Abstract for rendering Barbarians.
 */
public abstract class AbstractRendererBarbarian<T extends AbstractEntityBarbarian, M extends BipedModel<T>> extends BipedRenderer<T, M>
{
    public AbstractRendererBarbarian(final EntityRendererManager renderManagerIn, final M modelBipedIn, final float shadowSize)
    {
        super(renderManagerIn, (M) new BipedModel(0.0F), shadowSize);
        this.addLayer(new HeldItemLayer(this));
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
    }
}