package com.minecolonies.coremod.client.render.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary extends MobRenderer<CreatureEntity, BipedModel<CreatureEntity>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/settlermale1.png");

    /**
     * Renders the mercenary mobs, with an held item and armorset.
     *
     * @param renderManagerIn RenderManager
     */
    public RenderMercenary(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new BipedModel<>(1.0F), 0.5f);

        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(1.0F), new BipedModel<>(1.0F)));
    }

    @Override
    public ResourceLocation getEntityTexture(final CreatureEntity entity)
    {
        return TEXTURE;
    }
}
