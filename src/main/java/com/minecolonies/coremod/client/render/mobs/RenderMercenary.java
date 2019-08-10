package com.minecolonies.coremod.client.render.mobs;

import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary<T extends EntityMercenary, M extends BipedModel<T>> extends BipedRenderer<T, M>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/settlermale1.png");

    /**
     * Renders the mercenary mobs, with an held item and armorset.
     *
     * @param renderManagerIn RenderManager
     */
    public RenderMercenary(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, (M) new BipedModel(1.0F), 0.5f);

        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(1.0F), new BipedModel(1.0F)));
    }

    @Override
    protected ResourceLocation getEntityTexture(final T entity)
    {
        return TEXTURE;
    }
}
