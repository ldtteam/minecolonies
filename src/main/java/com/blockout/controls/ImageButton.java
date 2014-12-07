package com.blockout.controls;

import com.blockout.PaneParams;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ImageButton extends Button
{
    private static final ResourceLocation soundClick = new ResourceLocation("gui.button.press");
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;

    protected int imageOffsetX = 0, imageOffsetY = 0, imageWidth = 0, imageHeight = 0;
    protected int highlightOffsetX = 0, highlightOffsetY = 0, highlightWidth = 0, highlightHeight = 0;

    public ImageButton()
    {
        setSize(20, 20);
    }

    public ImageButton(PaneParams params)
    {
        super(params);

        String path = params.getStringAttribute("source", null);
        if (path != null)
        {
            image = new ResourceLocation(path);
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        imageOffsetX = size.width;
        imageOffsetY = size.height;

        size = params.getSizePairAttribute("imagesize", null, null);
        imageWidth = size.width;
        imageHeight = size.height;

        path = params.getStringAttribute("highlight", null);
        if (path != null)
        {
            imageHighlight = new ResourceLocation(path);
        }

        size = params.getSizePairAttribute("highlightoffset", null, null);
        highlightOffsetX = size.width;
        highlightOffsetY = size.height;

        size = params.getSizePairAttribute("highlightsize", null, null);
        highlightWidth = size.width;
        highlightHeight = size.height;
    }

    public void setImage(String source, int offsetX, int offsetY, int w, int h)
    {
        setImage(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    public void setImage(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageHeight = w;
        imageWidth = h;
    }

    public void setImageHighlight(String source, int offsetX, int offsetY, int w, int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    public void setImageHighlight(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        imageHighlight = loc;
        highlightOffsetX = offsetX;
        highlightOffsetY = offsetY;
        highlightHeight = w;
        highlightWidth = h;
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        ResourceLocation bind = image;
        int offsetX = imageOffsetX;
        int offsetY = imageOffsetY;
        int w = imageWidth;
        int h = imageHeight;

        if (imageHighlight != null && isPointInPane(mx, my))
        {
            bind = imageHighlight;
            offsetX = highlightOffsetX;
            offsetY = highlightOffsetY;
            w = highlightWidth;
            h = highlightHeight;
        }

        if (w == 0 || w > getWidth())   w = getWidth();
        if (h == 0 || h > getHeight())  h = getHeight();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(bind);
        drawTexturedModalRect(x, y,
                offsetX, offsetY,
                w, h);
    }

    @Override
    public void handleClick(int mx, int my)
    {
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(soundClick, 1.0F));
        super.handleClick(mx, my);
    }
}
