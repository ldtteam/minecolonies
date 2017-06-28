package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.ai.mobs.EntityBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RenderBarbarianMob extends RenderLiving<EntityBarbarian>
{
    private final ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");

    private static final float SHADOW_SIZE = 0.5F;

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RenderBarbarianMob(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final EntityBarbarian entity)
    {
        return mobTexture;
    }
}