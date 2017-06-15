package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.client.model.ModelBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityArcherBarbarian;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

/**
 * Created by Asher on 5/6/17.
 */
public class RenderArcherBarbarianMob extends RenderLiving<EntityArcherBarbarian>
{
    private final ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");

    private static final float SHADOW_SIZE = 0.5F;

    public static final RenderArcherBarbarianMob.Factory FACTORY = new RenderArcherBarbarianMob.Factory();

    public RenderArcherBarbarianMob(final RenderManager renderManagerIn)
    {
        // We use the vanilla zombie model here and we simply
        // re-texture it. Of course you can make your own model
        super(renderManagerIn, new ModelBarbarian(), SHADOW_SIZE);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
        // 0.0F, false
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final EntityArcherBarbarian entity)
    {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityArcherBarbarian>
    {

        @Override
        public Render<? super EntityArcherBarbarian> createRenderFor(final RenderManager manager)
        {
            return new RenderArcherBarbarianMob(manager);
        }
    }
}
