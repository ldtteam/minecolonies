package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Abstract for rendering Barbarians.
 */
public abstract class AbstractRendererBarbarian extends RenderLiving<AbstractEntityBarbarian>
{
    private static final float SHADOW_SIZE = 0.5F;
    private final ResourceLocation mobTexture;

    /**
     * Constructor method for renderer.
     *
     * @param barbarian       the barbarian class to render.
     * @param renderManagerIn the renderManager
     */
    public AbstractRendererBarbarian(final RenderManager renderManagerIn, final Class barbarian)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);

        if (barbarian == EntityBarbarian.class)
        {
            mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");
        }
        else if (barbarian == EntityChiefBarbarian.class)
        {
            mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarianchief1.png");
        }
        else
        {
            mobTexture = null;
        }

        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final AbstractEntityBarbarian entity)
    {
        return mobTexture;
    }
}