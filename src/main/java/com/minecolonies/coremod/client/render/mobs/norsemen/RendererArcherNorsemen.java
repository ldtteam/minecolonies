package com.minecolonies.coremod.client.render.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.coremod.client.model.raiders.ModelArcherNorsemen;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererArcherNorsemen extends AbstractRendererNorsemen<AbstractEntityNorsemen, ModelArcherNorsemen>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_archer1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_archer2.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererArcherNorsemen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelArcherNorsemen(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityNorsemen entity)
    {
        if (entity.getTextureId() == 1)
        {
            return TEXTURE2;
        }
        return TEXTURE1;
    }
}
