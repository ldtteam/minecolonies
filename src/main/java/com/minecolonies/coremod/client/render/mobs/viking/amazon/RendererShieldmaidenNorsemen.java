package com.minecolonies.coremod.client.render.mobs.viking.amazon;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.coremod.client.model.raiders.VikingShieldmaiden;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererShieldmaidenNorsemen extends AbstractRendererNorsemen
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_shieldmaiden1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_shieldmaiden2.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererShieldmaidenNorsemen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new VikingShieldmaiden(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        if (((AbstractEntityNorsemen) entity).getTextureId() == 1)
        {
            return TEXTURE2;
        }
        return TEXTURE1;
    }
}
