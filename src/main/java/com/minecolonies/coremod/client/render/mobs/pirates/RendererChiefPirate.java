package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefPirate extends AbstractRendererPirate<AbstractEntityPirate, BipedModel<AbstractEntityPirate>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate_nude1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate_nude2.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate_nude3.png");
    private static final ResourceLocation TEXTURE4 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate_nude4.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefPirate(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<>(0.0F), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityPirate entity)
    {
        switch (entity.getTextureId())
        {
            case 0:
                return TEXTURE1;
            case 1:
                return TEXTURE2;
            case 2:
                return TEXTURE3;
            default:
                return TEXTURE4;
        }
    }
}
