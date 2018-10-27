package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityChiefPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Abstract for rendering Pirates.
 */
public abstract class AbstractRendererPirate extends RenderLiving<AbstractEntityPirate>
{
    private static final float SHADOW_SIZE = 0.5F;
    private final ResourceLocation mobTexture;

    /**
     * Constructor method for renderer.
     *
     * @param pirate       the pirate class to render.
     * @param renderManagerIn the renderManager
     */
    public AbstractRendererPirate(final RenderManager renderManagerIn, final Class pirate)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);

        if (pirate == EntityArcherPirate.class)
        {
            mobTexture = new ResourceLocation("minecolonies:textures/entity/pirate2.png");
        }
        else if (pirate == EntityPirate.class)
        {
            mobTexture = new ResourceLocation("minecolonies:textures/entity/pirate1.png");
        }
        else if (pirate == EntityChiefPirate.class)
        {
            mobTexture = new ResourceLocation("minecolonies:textures/entity/pirate_nude.png");
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
    protected ResourceLocation getEntityTexture(@Nonnull final AbstractEntityPirate entity)
    {
        return mobTexture;
    }
}