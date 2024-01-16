package com.minecolonies.core.client.render.projectile;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for the fire arrows.
 */
public class FireArrowRenderer extends ArrowRenderer<AbstractArrow>
{
    /**
     * Array of different textures.
     */
    private static final ResourceLocation[] RES = new ResourceLocation[]
                                                    {
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow1.png"),
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow2.png"),
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow3.png"),
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow4.png"),
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow5.png"),
                                                      new ResourceLocation(Constants.MOD_ID, "textures/items/magicalarrows/magical_arrow6.png")
                                                    };

    public FireArrowRenderer(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull final AbstractArrow entity)
    {
        return RES[entity.tickCount % 6];
    }
}
