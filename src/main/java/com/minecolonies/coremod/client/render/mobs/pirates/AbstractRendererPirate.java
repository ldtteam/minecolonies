package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;

/**
 * Abstract for rendering Pirates.
 */
public abstract class AbstractRendererPirate extends RenderLiving<AbstractEntityPirate>
{
    /**
     * Shadow size of entity.
     */
    private static final float SHADOW_SIZE = 0.5F;

    /**
     * Constructor method for renderer.
     *
     * @param renderManagerIn the renderManager
     */
    public AbstractRendererPirate(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }
}