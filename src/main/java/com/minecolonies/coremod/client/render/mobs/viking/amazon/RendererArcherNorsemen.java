package com.minecolonies.coremod.client.render.mobs.viking.amazon;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.coremod.client.model.raiders.ArcherNorsemen;
import com.minecolonies.coremod.client.model.raiders.ModelAmazon;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererArcherNorsemen extends AbstractRendererNorsemen
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_archer1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_archer2.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation("minecolonies:textures/entity/raiders/norsemen_archer3.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererArcherNorsemen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ArcherNorsemen(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        switch (((AbstractEntityNorsemen) entity).getTextureId())
        {
            case 1:
                return TEXTURE2;
            case 2:
                return TEXTURE3;
            default:
                return TEXTURE1;
        }
    }
}
