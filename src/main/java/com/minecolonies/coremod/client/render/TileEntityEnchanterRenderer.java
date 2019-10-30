package com.minecolonies.coremod.client.render;

import com.minecolonies.api.tileentities.TileEntityEnchanter;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityEnchanterRenderer extends TileEntitySpecialRenderer<TileEntityEnchanter>
{
    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("minecolonies:textures/blocks/enchanting_table_book.png");
    private final        ModelBook        modelBook    = new ModelBook();

    public TileEntityEnchanterRenderer()
    {
    }

    @Override
    public void render(TileEntityEnchanter entity, double x, double y, double z, float partialTick, int rot, float jaw)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);
        float tick = (float) entity.tickCount + partialTick;
        GlStateManager.translate(0.0F, 0.1F + MathHelper.sin(tick * 0.1F) * 0.01F, 0.0F);

        double rotVPrev = entity.bookRotation - entity.bookRotationPrev;
        float var = (float) ((rotVPrev + Math.PI % (2 * Math.PI)) - Math.PI);

        float lvt_13_1_ = entity.bookRotationPrev + var * partialTick;
        GlStateManager.rotate(-lvt_13_1_ * 57.295776F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/blocks/enchanting_table_book.png"));
        float lvt_14_1_ = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTick + 0.25F;
        float lvt_15_1_ = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTick + 0.75F;
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

        float lvt_16_1_ = entity.bookSpreadPrev + (entity.bookSpread - entity.bookSpreadPrev) * partialTick;
        GlStateManager.enableCull();
        this.modelBook.render((Entity) null, tick, lvt_14_1_, lvt_15_1_, lvt_16_1_, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
