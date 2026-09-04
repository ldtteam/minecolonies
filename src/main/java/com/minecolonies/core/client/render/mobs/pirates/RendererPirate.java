package com.minecolonies.core.client.render.mobs.pirates;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererPirate extends AbstractRendererPirate<AbstractEntityMinecoloniesMonster, HumanoidModel<RaiderRenderState>>
{
    /**
     * Texture of the entity.
     */
    private static final Identifier TEXTURE1 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/pirate1.png");
    private static final Identifier TEXTURE2 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/pirate2.png");
    private static final Identifier TEXTURE3 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/pirate3.png");
    private static final Identifier TEXTURE4 = Identifier.fromNamespaceAndPath("minecolonies", "textures/entity/raiders/pirate4.png");

    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererPirate(final EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(RaiderRenderState entity)
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
