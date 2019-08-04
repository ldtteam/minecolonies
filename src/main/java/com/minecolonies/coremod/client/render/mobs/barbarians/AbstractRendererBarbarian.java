package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;

/**
 * Abstract for rendering Barbarians.
 */
public abstract class AbstractRendererBarbarian extends RenderLiving<AbstractEntityBarbarian>
{
    /**
     * Shadow size of the entity.
     */
    private static final float SHADOW_SIZE = 0.5F;

    /**
     * Constructor method for renderer.
     *
     * @param renderManagerIn the renderManager
     */
    public AbstractRendererBarbarian(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }
}