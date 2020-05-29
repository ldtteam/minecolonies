package com.minecolonies.coremod.client.render.mobs.viking.amazon;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.coremod.client.model.raiders.ChiefNorsemen;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief amazons.
 */
public class RendererChiefNorsemen extends AbstractRendererNorsemen
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_chief.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefNorsemen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ChiefNorsemen(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        return TEXTURE;
    }
}
