package com.minecolonies.core.client.render.mobs.amazon;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.model.raiders.ModelAmazon;
import com.minecolonies.core.event.ClientRegistryHandler;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for archer amazons.
 */
public class RendererAmazon extends AbstractRendererAmazon<AbstractEntityMinecoloniesMonster, ModelAmazon>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/amazon.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererAmazon(final EntityRendererProvider.Context context)
    {
        super(context, new ModelAmazon(context.bakeLayer(ClientRegistryHandler.AMAZON)), 0.5F);
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(@NotNull final RaiderRenderState entity)
    {
        return TEXTURE;
    }
}
