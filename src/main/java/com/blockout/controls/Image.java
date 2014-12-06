package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Image extends Pane
{
    protected ResourceLocation image;
    int imageX = 0;
    int imageY = 0;
    int imageWidth = 0;
    int imageHeight = 0;

    public Image() { super(); }
    public Image(Image img) { super(img); }
    public Image(PaneParams params)
    {
        super(params);
        String path = params.getStringAttribute("source", null);
        if (path != null)
        {
            image = new ResourceLocation(path);
        }
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(image);
        drawTexturedModalRect(x, y,
                imageX, imageY,
                imageWidth != 0 ? imageWidth : getWidth(), imageHeight != 0 ? imageHeight : getHeight());
    }
}
