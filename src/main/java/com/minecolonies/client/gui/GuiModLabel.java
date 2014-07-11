package com.minecolonies.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiModLabel
{
    private final String text;
    private final int    x, y;
    private int color;

    public GuiModLabel(String text, int x, int y, int color)
    {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public void drawLabel(Minecraft mc)
    {
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(text, x, y, color);
    }
}
