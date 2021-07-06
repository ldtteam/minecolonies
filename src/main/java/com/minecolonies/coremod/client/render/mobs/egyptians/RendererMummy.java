package com.minecolonies.coremod.client.render.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.coremod.client.model.raiders.ModelMummy;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer used for mummies.
 */
public class RendererMummy extends AbstractRendererEgyptian<AbstractEntityEgyptian, ModelMummy>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/mummy.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererMummy(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelMummy(), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityEgyptian entity)
    {
        return TEXTURE;
    }
}
