package com.minecolonies.core.client.render.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererBarbarian extends AbstractRendererBarbarian<AbstractEntityBarbarian, HumanoidModel<AbstractEntityBarbarian>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies", "textures/entity/raiders/barbarian1.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererBarbarian(final EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), 0.5F);
    }


    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityBarbarian entity)
    {
        return TEXTURE;
    }
}
