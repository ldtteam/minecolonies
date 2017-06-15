package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.ai.mobs.EntityBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

/**
 * Created by Asher on 5/6/17.
 */
public class RenderBarbarianMob extends RenderLiving<EntityBarbarian>
{
    private final ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");

    private static final float SHADOW_SIZE = 0.5F;

    public static final Factory FACTORY = new Factory();

    public RenderBarbarianMob(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBiped(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final EntityBarbarian entity)
    {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityBarbarian>
    {

        @Override
        public Render<? super EntityBarbarian> createRenderFor(final RenderManager manager)
        {
            return new RenderBarbarianMob(manager);
        }
    }
}
