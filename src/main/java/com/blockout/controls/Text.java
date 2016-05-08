package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Text extends Pane
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");
    protected String    text;
    protected List<String> formattedText;
    protected int       textHeight;
    protected int       linespace     = 0;
    protected float     scale         = 1.0f;
    protected Alignment textAlignment = Alignment.TopLeft;
    protected int       textColor     = 0xffffff;
    protected boolean   shadow        = false;

    public Text(){}

    public Text(PaneParams params)
    {
        super(params);

        text          = params.getLocalizedText();
        linespace     = params.getIntegerAttribute("linespace", linespace);
        scale         = params.getFloatAttribute("textscale", scale);
        textAlignment = params.getEnumAttribute("textalign", textAlignment);
        textColor     = params.getColorAttribute("color", textColor);
        shadow        = params.getBooleanAttribute("shadow", shadow);
    }

    public String getText() { return text; }
    public void setText(String s)
    {
        text = s;
        formattedText = null;
    }

    public int getLineSpace() { return linespace; }
    public void setLineSpace(int l) { linespace = l; }

    public int getColor() { return textColor; }
    public void setColor(int c) { textColor = c; }

    public boolean getShadow() { return shadow; }
    public void setShadow(boolean s) { shadow = s; }

    public Alignment getTextAlignment() { return textAlignment; }
    public void setTextAlignment(Alignment align) { textAlignment = align; }

    public float getScale() { return scale; }
    public void setScale(float s)
    {
        scale = s;
        formattedText = null;
    }

    public int getLineHeight() { return (int)(mc.fontRendererObj.FONT_HEIGHT * scale); }

    public int getTextHeight()
    {
        getFormattedText(); //  Force computation of textHeight, if necessary
        return textHeight;
    }

    public int getStringWidth(String s) { return (int)(mc.fontRendererObj.getStringWidth(s) * scale); }

    public List<String> getFormattedText()
    {
        if (formattedText == null)
        {
            if (text == null || text.length() == 0)
            {
                formattedText = Collections.unmodifiableList(new ArrayList<String>());
            }
            else
            {
                formattedText = Collections.unmodifiableList(mc.fontRendererObj.listFormattedStringToWidth(text, (int)(getWidth() / scale)));
            }

            int numLines = getFormattedText().size();
            if (numLines > 0)
            {
                int scaledLinespace = (int)(linespace * scale);
                textHeight = (numLines * (getLineHeight() + scaledLinespace)) - scaledLinespace;
            }
            else
            {
                textHeight = 0;
            }
        }

        return formattedText;
    }

    @Override
    public void drawSelf(int mx, int my)
    {
        int scaledLinespace = (int)(linespace * scale);
        int offsetY = 0;

        if (textAlignment.bottomAligned || textAlignment.verticalCentered)
        {
            int maxVisibleLines = (getHeight() + scaledLinespace) / (getLineHeight() + scaledLinespace);
            int maxVisibleSize = (maxVisibleLines * (getLineHeight() + scaledLinespace)) - scaledLinespace;

            if (getTextHeight() < maxVisibleSize)
            {
                maxVisibleSize = getTextHeight();
            }

            offsetY = Math.max(0, (getHeight() - maxVisibleSize));

            if (textAlignment.verticalCentered)
            {
                offsetY = offsetY / 2;
            }
        }

        for (String s : getFormattedText())
        {
            if (s == null) break;

            int offsetX = 0;
            if (textAlignment.rightAligned || textAlignment.horizontalCentered)
            {
                offsetX = (getWidth() - getStringWidth(s));

                if (textAlignment.horizontalCentered)
                {
                    offsetX = offsetX / 2;
                }
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(getX() + offsetX, getY() + offsetY, 0);
            GL11.glScalef(scale, scale, scale);
            mc.renderEngine.bindTexture(TEXTURE);
            mc.fontRendererObj.drawString(s, 0, 0, textColor, shadow);
            GL11.glPopMatrix();

            offsetY += getLineHeight() + scaledLinespace;

            if ((offsetY + getLineHeight()) > getHeight())
            {
                break;
            }
        }
    }
}
