package com.minecolonies.coremod.client.render;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for the fire arrows.
 */
public class FireArrowRenderer extends ArrowRenderer<AbstractArrowEntity>
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

    public FireArrowRenderer(EntityRendererManager manager)
    {
        super(manager);
    }

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(@NotNull final AbstractArrowEntity entity)
    {
        return RES[entity.ticksExisted % 6];
    }
}
