package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.PaneParams;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ButtonImage extends Button
{
    private static final ResourceLocation soundClick = new ResourceLocation("gui.button.press");
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;

    protected int       imageOffsetX = 0, imageOffsetY = 0, imageWidth = 0, imageHeight = 0;
    protected int       highlightOffsetX = 0, highlightOffsetY = 0, highlightWidth = 0, highlightHeight = 0;
    protected Alignment textAlignment     = Alignment.Middle;
    protected int       textColor         = 0xffffff;
    protected int       textHoverColor    = 0xffffff;
    protected int       textDisabledColor = 0xffffff;
    protected boolean   shadow            = false;
    protected int       textOffsetX = 0, textOffsetY = 0;

    public ButtonImage()
    {
        setSize(20, 20);
    }

    public ButtonImage(PaneParams params)
    {
        super(params);

        String path = params.getStringAttribute("source", null);
        if (path != null)
        {
            image = new ResourceLocation(path);
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        if (size != null)
        {
            imageOffsetX = size.x;
            imageOffsetY = size.y;
        }

        size = params.getSizePairAttribute("imagesize", null, null);
        if (size != null)
        {
            imageWidth = size.x;
            imageHeight = size.y;
        }

        path = params.getStringAttribute("highlight", null);
        if (path != null)
        {
            imageHighlight = new ResourceLocation(path);
        }

        size = params.getSizePairAttribute("highlightoffset", null, null);
        if (size != null)
        {
            highlightOffsetX = size.x;
            highlightOffsetY = size.y;
        }

        size = params.getSizePairAttribute("highlightsize", null, null);
        if (size != null)
        {
            highlightWidth = size.x;
            highlightHeight = size.y;
        }

        textAlignment     = params.getEnumAttribute("textalign", textAlignment);
        textColor         = params.getColorAttribute("textcolor", textColor);
        textHoverColor    = params.getColorAttribute("texthovercolor", textColor); //  match textcolor by default
        textDisabledColor = params.getColorAttribute("textdisabledcolor", textColor); //  match textcolor by default
        shadow            = params.getBooleanAttribute("shadow", shadow);

        size = params.getSizePairAttribute("textoffset", null, null);
        if (size != null)
        {
            textOffsetX = size.x;
            textOffsetY = size.y;
        }
    }

    public void setImage(String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    public void setImage(String source, int offsetX, int offsetY, int w, int h)
    {
        setImage(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    public void setImage(ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    public void setImage(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageHeight = w;
        imageWidth = h;
    }

    public void setImageHighlight(String source)
    {
        setImageHighlight(source, 0, 0, 0, 0);
    }

    public void setImageHighlight(String source, int offsetX, int offsetY, int w, int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    public void setImageHighlight(ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
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

        boolean mouseOver = isPointInPane(mx, my);

        if (mouseOver && imageHighlight != null)
        {
            bind = imageHighlight;
            offsetX = highlightOffsetX;
            offsetY = highlightOffsetY;
            w = highlightWidth;
            h = highlightHeight;
        }

        if (w == 0 || w > getWidth())   w = getWidth();
        if (h == 0 || h > getHeight())  h = getHeight();

        mc.renderEngine.bindTexture(bind);
        if (enabled)
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
        }

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawTexturedModalRect(x, y, offsetX, offsetY, w, h);

        //  Label, if any
        if (label != null)
        {
            int color = enabled ? (mouseOver ? textHoverColor : textColor) : textDisabledColor;

            offsetX = textOffsetX;
            offsetY = textOffsetY;

            if (textAlignment.rightAligned)
            {
                offsetX += (getWidth() - mc.fontRenderer.getStringWidth(label));
            }
            else if (textAlignment.horizontalCentered)
            {
                offsetX += (getWidth() - mc.fontRenderer.getStringWidth(label)) / 2;
            }

            if (textAlignment.bottomAligned)
            {
                offsetY += (getHeight() - mc.fontRenderer.FONT_HEIGHT);
            }
            else if (textAlignment.verticalCentered)
            {
                offsetY += (getHeight() - mc.fontRenderer.FONT_HEIGHT) / 2;
            }

            mc.fontRenderer.drawString(label, getX() + offsetX, getY() + offsetY, color, shadow);
        }
    }

    @Override
    public void handleClick(int mx, int my)
    {
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(soundClick, 1.0F));
        super.handleClick(mx, my);
    }
}
