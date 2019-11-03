package com.minecolonies.coremod.client.render;

import com.minecolonies.api.tileentities.TileEntityEnchanter;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TileEntityEnchanterRenderer extends TileEntityRenderer<TileEntityEnchanter>
{
    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("minecolonies:textures/blocks/enchanting_table_book.png");
    private final        BookModel        modelBook    = new BookModel();

    public TileEntityEnchanterRenderer()
    {
    }

    @Override
    public void render(final TileEntityEnchanter entity, final double x, final double y, final double z, final float partialTicks, final int destroyStage)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translated((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);
        float tick = (float) entity.tickCount + partialTicks;
        GlStateManager.translated(0.0F, 0.1F + MathHelper.sin(tick * 0.1F) * 0.01F, 0.0F);

        double rotVPrev = entity.bookRotation - entity.bookRotationPrev;
        float var = (float) ((rotVPrev + Math.PI % (2 * Math.PI)) - Math.PI);

        float lvt_13_1_ = entity.bookRotationPrev + var * partialTicks;
        GlStateManager.rotated(-lvt_13_1_ * 57.295776F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotated(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/blocks/enchanting_table_book.png"));
        float lvt_14_1_ = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTicks + 0.25F;
        float lvt_15_1_ = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTicks + 0.75F;
        lvt_14_1_ = (lvt_14_1_ - (float) MathHelper.fastFloor((double) lvt_14_1_)) * 1.6F - 0.3F;
        lvt_15_1_ = (lvt_15_1_ - (float) MathHelper.fastFloor((double) lvt_15_1_)) * 1.6F - 0.3F;
        if (lvt_14_1_ < 0.0F)
        {
            lvt_14_1_ = 0.0F;
        }

        if (lvt_15_1_ < 0.0F)
        {
            lvt_15_1_ = 0.0F;
        }

        if (lvt_14_1_ > 1.0F)
        {
            lvt_14_1_ = 1.0F;
        }

        if (lvt_15_1_ > 1.0F)
        {
            lvt_15_1_ = 1.0F;
        }

        float lvt_16_1_ = entity.bookSpreadPrev + (entity.bookSpread - entity.bookSpreadPrev) * partialTicks;
        GlStateManager.enableCull();
        this.modelBook.render(tick, lvt_14_1_, lvt_15_1_, lvt_16_1_, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
