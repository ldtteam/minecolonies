package com.minecolonies.coremod.client.render;

import com.minecolonies.api.tileentities.TileEntityEnchanter;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityEnchanterRenderer extends TileEntitySpecialRenderer<TileEntityEnchanter>
{
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
        float circleRot = (float) ((rotVPrev + Math.PI % (2 * Math.PI)) - Math.PI);

        float tickBasedRot = entity.bookRotationPrev + circleRot * partialTick;
        GlStateManager.rotate(-tickBasedRot * 57.295776F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(new ResourceLocation(Constants.MOD_ID, "textures/blocks/enchanting_table_book.png"));
        float page1 = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTick + 0.25F;
        float page2 = entity.pageFlipPrev + (entity.pageFlip - entity.pageFlipPrev) * partialTick + 0.75F;
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

        float bookSpread = entity.bookSpreadPrev + (entity.bookSpread - entity.bookSpreadPrev) * partialTick;
        GlStateManager.enableCull();
        this.modelBook.render(null, tick, page1, page2, bookSpread, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
