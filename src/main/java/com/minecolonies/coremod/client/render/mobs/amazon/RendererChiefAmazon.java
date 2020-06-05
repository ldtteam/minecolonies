package com.minecolonies.coremod.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.coremod.client.model.raiders.ModelAmazonChief;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief amazons.
 */
public class RendererChiefAmazon extends AbstractRendererAmazon<AbstractEntityAmazon, ModelAmazonChief>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecolonies:textures/entity/raiders/amazon_chief.png");

    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefAmazon(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ModelAmazonChief(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityAmazon entity)
    {
        return TEXTURE;
    }
}
