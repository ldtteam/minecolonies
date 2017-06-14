package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import net.minecraft.client.model.ModelBiped;
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
public class RenderChiefBarbarianMob extends RenderLiving<EntityChiefBarbarian>
{
    /* default */ private final ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/chiefbarbarian1.png");

    /* default */ private static final float SHADOW_SIZE = 0.5F;

    public static final RenderChiefBarbarianMob.Factory FACTORY = new RenderChiefBarbarianMob.Factory();

    public RenderChiefBarbarianMob(final RenderManager renderManagerIn)
    {
        // We use the vanilla zombie model here and we simply
        // re-texture it. Of course you can make your own model
        super(renderManagerIn, new ModelBiped(), 0.5F);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull final EntityChiefBarbarian entity)
    {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityChiefBarbarian>
    {

        @Override
        public Render<? super EntityChiefBarbarian> createRenderFor(final RenderManager manager)
        {
            return new RenderChiefBarbarianMob(manager);
        }
    }
}
