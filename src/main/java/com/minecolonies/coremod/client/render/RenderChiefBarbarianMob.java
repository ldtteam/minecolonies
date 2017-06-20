package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RenderChiefBarbarianMob extends RenderLiving<EntityChiefBarbarian>
{
    private final ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarianchief1.png");

    private static final float SHADOW_SIZE = 0.5F;

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RenderChiefBarbarianMob(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final EntityChiefBarbarian entity)
    {
        return mobTexture;
    }
}