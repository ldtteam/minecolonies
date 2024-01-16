package com.minecolonies.core.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererPirate extends AbstractRendererPirate<AbstractEntityPirate, HumanoidModel<AbstractEntityPirate>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate2.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate3.png");
    private static final ResourceLocation TEXTURE4 = new ResourceLocation("minecolonies:textures/entity/raiders/pirate4.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererPirate(final EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityPirate entity)
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
