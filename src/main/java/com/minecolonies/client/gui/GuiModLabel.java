package com.minecolonies.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiModLabel {
    protected String text;
    protected int x, y;

    public GuiModLabel(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public void drawLabel(Minecraft mc) {
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(text, x, y, 0x000000);
    }
}
