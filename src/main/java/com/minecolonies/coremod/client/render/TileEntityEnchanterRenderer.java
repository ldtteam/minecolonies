package com.minecolonies.coremod.client.render;

import com.minecolonies.api.tileentities.TileEntityEnchanter;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.entity.model.BookModel;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TileEntityEnchanterRenderer extends TileEntityRenderer<TileEntityEnchanter>
{
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
        float circleRot = (float) ((rotVPrev + Math.PI % (2 * Math.PI)) - Math.PI);

        float tickBasedRot = entity.bookRotationPrev + circleRot * partialTicks;
        GlStateManager.rotated(-tickBasedRot * 57.295776F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotated(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/blocks/enchanting_table_book.png"));
        float page1 = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTicks + 0.25F;
        float page2 = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTicks + 0.75F;
        page1 = (page1 - (float) MathHelper.fastFloor((double) page1)) * 1.6F - 0.3F;
        page2 = (page2 - (float) MathHelper.fastFloor((double) page2)) * 1.6F - 0.3F;
        if (page1 < 0.0F)
        {
            page1 = 0.0F;
        }

        if (page2 < 0.0F)
        {
            page2 = 0.0F;
        }

        if (page1 > 1.0F)
        {
            page1 = 1.0F;
        }

        if (page2 > 1.0F)
        {
            page2 = 1.0F;
        }

        float bookSpread = entity.bookSpreadPrev + (entity.bookSpread - entity.bookSpreadPrev) * partialTicks;
        GlStateManager.enableCull();
        this.modelBook.render(tick, page1, page2, bookSpread, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
