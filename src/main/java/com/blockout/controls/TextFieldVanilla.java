package com.blockout.controls;

import com.blockout.PaneParams;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.opengl.GL11;

/**
 * Mimics Vanilla text fields.
 */
public class TextFieldVanilla extends TextField
{
    protected boolean backgroundEnabled    = true;
    protected int     backgroundOuterColor = 0xFFA0A0A0;
    protected int     backgroundInnerColor = 0xFF000000;

    /**
     * Required defualt constructor.
     */
    public TextFieldVanilla()
    {
        filter = new FilterVanilla();
    }

    /**
     * Constructor called when creating an object from xml.
     *
     * @param params xml parameters.
     */
    public TextFieldVanilla(PaneParams params)
    {
        super(params);
        backgroundEnabled    = params.getBooleanAttribute("background", backgroundEnabled);
        backgroundOuterColor = params.getColorAttribute("backgroundOuter", backgroundOuterColor);
        backgroundInnerColor = params.getColorAttribute("backgroundInner", backgroundInnerColor);
        filter = new FilterVanilla();
    }

    public boolean getBackgroundEnabled()
    {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(boolean e)
    {
        backgroundEnabled = e;
    }

    public int getBackgroundOuterColor()
    {
        return backgroundOuterColor;
    }

    public void setBackgroundOuterColor(int c)
    {
        backgroundOuterColor = c;
    }

    public int getBackgroundInnerColor()
    {
        return backgroundInnerColor;
    }

    public void setBackgroundInnerColor(int c)
    {
        backgroundInnerColor = c;
    }

    @Override
    public int getInternalWidth()
    {
        return backgroundEnabled ? (getWidth() - 8) : getWidth();
    }

    @Override
    public void drawSelf(int mx, int my)
    {
        if (backgroundEnabled)
        {
            //  Draw box
            drawRect(x - 1, y - 1, x + width + 1, y + height + 1, backgroundOuterColor);
            drawRect(x, y, x + width, y + height, backgroundInnerColor);

            GL11.glPushMatrix();
            GL11.glTranslatef(4, (float) ((height - 8) / 2.0), 0);
        }

        super.drawSelf(mx, my);

        if (backgroundEnabled)
        {
            GL11.glPopMatrix();
        }
    }

    @Override
    public void handleClick(int mx, int my)
    {
        int mouseX = mx;

        if (backgroundEnabled)
        {
            mouseX -= 4;
        }

        super.handleClick(mouseX, my);
    }

    private static class FilterNumeric implements Filter
    {
        @Override
        public String filter(String s)
        {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray())
            {
                if (isAllowedCharacter(c))
                {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        @Override
        public boolean isAllowedCharacter(char c)
        {
            return Character.isDigit(c);
        }
    }

    private static class FilterVanilla implements Filter
    {
        @Override
        public String filter(String s)
        {
            return ChatAllowedCharacters.filterAllowedCharacters(s);
        }

        @Override
        public boolean isAllowedCharacter(char c)
        {
            return ChatAllowedCharacters.isAllowedCharacter(c);
        }
    }
}
