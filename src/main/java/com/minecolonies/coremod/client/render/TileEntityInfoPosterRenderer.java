package com.minecolonies.coremod.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TileEntityInfoPosterRenderer extends TileEntitySpecialRenderer<TileEntityInfoPoster>
{
    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/blocks/info/info_poster.png");
    /**
     * The ModelSign instance for use in this renderer
     */
    private final        ModelSign        model        = new ModelSign();

    @Override
    public void renderTileEntityAt(final TileEntityInfoPoster te, final double x, final double y, final double z, final float partialTicks, final int destroyStage)
    {
        GlStateManager.pushMatrix();
        int k = te.getBlockMetadata();
        float f2 = 0.0F;

        if (k == 2)
        {
            f2 = 180.0F;
        }

        if (k == 4)
        {
            f2 = 90.0F;
        }

        if (k == 5)
        {
            f2 = -90.0F;
        }

        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
        this.model.signStick.showModel = false;

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
        else
        {
            this.bindTexture(SIGN_TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
        this.model.renderSign();
        GlStateManager.popMatrix();
        FontRenderer fontrenderer = this.getFontRenderer();
        float f3 = 0.010416667F;
        GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);

        if (destroyStage < 0)
        {
            for (int j = 0; j < te.signText.length; ++j)
            {
                if (te.signText[j] != null)
                {
                    ITextComponent itextcomponent = te.signText[j];
                    List<ITextComponent> list = GuiUtilRenderComponents.splitText(itextcomponent, 90, fontrenderer, false, true);
                    String s = list != null && !list.isEmpty() ? list.get(0).getFormattedText() : "";

                    if (j == te.lineBeingEdited)
                    {
                        s = "> " + s + " <";
                        fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
                    }
                    else
                    {
                        fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
                    }
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
