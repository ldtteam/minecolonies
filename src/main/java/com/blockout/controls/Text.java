package com.blockout.controls;

import com.blockout.PaneParams;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formatted larger textContent area.
 */
public class Text extends AbstractTextElement
{
    protected String textContent;
    protected List<String> formattedText;

    protected int textHeight;
    protected int linespace = 0;

    public Text()
    {
        // Required default constructor.
    }

    /**
     * Create text from xml.
     *
     * @param params xml parameters.
     */
    public Text(PaneParams params)
    {
        super(params);

        textContent = params.getLocalizedText();
        linespace = params.getIntegerAttribute("linespace", linespace);
    }

    @Override
    public void setScale(float s)
    {
        super.setScale(s);
        formattedText = null;
    }

    public String getTextContent()
    {
        return textContent;
    }

    public void setTextContent(String s)
    {
        textContent = s;
        formattedText = null;
    }

    public int getLineSpace()
    {
        return linespace;
    }

    public void setLineSpace(int l)
    {
        linespace = l;
    }

    public int getLineHeight()
    {
        return (int) (mc.fontRendererObj.FONT_HEIGHT * scale);
    }

    public int getTextHeight()
    {
        // Force computation of textHeight, if necessary
        getFormattedText();
        return textHeight;
    }

    /**
     * Find the width of the string.
     *
     * @param s string to calculated width of.
     * @return the width of the string, in pixels.
     */
    public int getStringWidth(String s)
    {
        return (int) (mc.fontRendererObj.getStringWidth(s) * scale);
    }

    public List<String> getFormattedText()
    {
        if (formattedText == null)
        {
            if (textContent == null || textContent.length() == 0)
            {
                formattedText = Collections.unmodifiableList(new ArrayList<String>());
            }
            else
            {
                formattedText = Collections.unmodifiableList(
                        mc.fontRendererObj.listFormattedStringToWidth(textContent, (int) (getWidth() / scale))
                                .stream()
                                .filter(s -> s != null)
                                .collect(Collectors.toList()));
            }

            int numLines = formattedText.size();
            if (numLines > 0)
            {
                int scaledLinespace = (int) (linespace * scale);
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
        int scaledLinespace = (int) (linespace * scale);
        int offsetY = 0;

        if (textAlignment.isBottomAligned() || textAlignment.isVerticalCentered())
        {
            int maxVisibleLines = (getHeight() + scaledLinespace) / (getLineHeight() + scaledLinespace);
            int maxVisibleSize = (maxVisibleLines * (getLineHeight() + scaledLinespace)) - scaledLinespace;

            if (getTextHeight() < maxVisibleSize)
            {
                maxVisibleSize = getTextHeight();
            }

            offsetY = Math.max(0, getHeight() - maxVisibleSize);

            if (textAlignment.isVerticalCentered())
            {
                offsetY = offsetY / 2;
            }
        }

        for (String s : getFormattedText())
        {
            int offsetX = 0;
            if (textAlignment.isRightAligned() || textAlignment.isHorizontalCentered())
            {
                offsetX = getWidth() - getStringWidth(s);

                if (textAlignment.isHorizontalCentered())
                {
                    offsetX /= 2;
                }
            }

            GL11.glPushMatrix();
            GL11.glTranslatef((float) (getX() + offsetX), (float) (getY() + offsetY), 0);
            GL11.glScalef((float) scale, (float) scale, (float) scale);
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
