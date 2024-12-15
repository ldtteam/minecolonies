package com.minecolonies.core.client.render.mobs.drownedpirates;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererDrownedChiefPirate extends AbstractRendererDrownedPirate<AbstractEntityMinecoloniesMob, HumanoidModel<AbstractEntityMinecoloniesMob>>
{
    /**
     * Texture of the entity.
     */
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("minecolonies:textures/entity/raiders/drowned_pirate_nude.png");


    /**
     * Constructor method for renderer
     *
     * @param context the renderManager
     */
    public RendererDrownedChiefPirate(final EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityMinecoloniesMob entity)
    {
        return TEXTURE1;
    }
}
