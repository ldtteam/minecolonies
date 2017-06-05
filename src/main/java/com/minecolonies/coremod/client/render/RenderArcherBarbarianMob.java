package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.client.model.ModelBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

/**
 * Created by Asher on 5/6/17.
 */
public class RenderArcherBarbarianMob extends RenderLiving<EntityArcherBarbarian>
{
    private ResourceLocation mobTexture = new ResourceLocation("minecolonies:textures/entity/barbarian1.png");

    public static final RenderArcherBarbarianMob.Factory FACTORY = new RenderArcherBarbarianMob.Factory();

    public RenderArcherBarbarianMob(RenderManager renderManagerIn) {
        // We use the vanilla zombie model here and we simply
        // re-texture it. Of course you can make your own model
        super(renderManagerIn, new ModelBarbarian(), 0.5F);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this));
        // 0.0F, false
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityArcherBarbarian entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityArcherBarbarian> {

        @Override
        public Render<? super EntityArcherBarbarian> createRenderFor(RenderManager manager) {
            return new RenderArcherBarbarianMob(manager);
        }

    }
}
