package com.minecolonies.coremod.client.render.mobs;

import com.minecolonies.coremod.entity.ai.mobs.EntityMercenary;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary extends RenderBiped<EntityMercenary>
{
    public RenderMercenary(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), 0.5f);

        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }

    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/settlermale1.png");

    @Override
    public ResourceLocation getEntityTexture(EntityMercenary entity)
    {
        return TEXTURE;
    }
}
