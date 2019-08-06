package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;

/**
 * Abstract for rendering Barbarians.
 */
public abstract class AbstractRendererBarbarian extends LivingRenderer<AbstractEntityBarbarian>
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
    public AbstractRendererBarbarian(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel(), SHADOW_SIZE);
        this.addLayer(new HeldItemLayer(this));
        this.addLayer(new BipedArmorLayer(this));
    }
}