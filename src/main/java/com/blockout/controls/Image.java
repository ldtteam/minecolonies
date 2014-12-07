package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Image extends Pane
{
    protected ResourceLocation image;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;

    public Image() { super(); }
    public Image(PaneParams params)
    {
        super(params);
        String source = params.getStringAttribute("source", null);
        if (source != null)
        {
            image = new ResourceLocation(source);
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        imageOffsetX = size.width;
        imageOffsetY = size.height;

        size = params.getSizePairAttribute("imagesize", null, null);
        imageWidth = size.width;
        imageHeight = size.height;
    }

    public void setImage(String source)
    {
        image = (source != null) ? new ResourceLocation(source) : null;
    }

    public void setImage(ResourceLocation loc)
    {
        image = loc;
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(image);
        drawTexturedModalRect(x, y,
                imageOffsetX, imageOffsetY,
                imageWidth != 0 ? imageWidth : getWidth(),
                imageHeight != 0 ? imageHeight : getHeight());
    }
}
