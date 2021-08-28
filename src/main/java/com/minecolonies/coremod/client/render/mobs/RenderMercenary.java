package com.minecolonies.coremod.client.render.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for EntityMercenary.
 */
public class RenderMercenary extends MobRenderer<PathfinderMob, HumanoidModel<PathfinderMob>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/citizen/default/settlermale1_b.png");

    /**
     * Renders the mercenary mobs, with an held item and armorset.
     *
     * @param renderManagerIn RenderManager
     */
    public RenderMercenary(final EntityRenderDispatcher renderManagerIn)
    {
        super(renderManagerIn, new HumanoidModel<>(1.0F), 0.5f);

        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(1.0F), new HumanoidModel<>(1.0F)));
    }

    @Override
    public ResourceLocation getTextureLocation(final PathfinderMob entity)
    {
        return TEXTURE;
    }
}
